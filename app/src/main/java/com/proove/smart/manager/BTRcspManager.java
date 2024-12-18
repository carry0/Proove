package com.proove.smart.manager;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.google.gson.Gson;
import com.jieli.bluetooth.bean.base.BaseError;
import com.jieli.bluetooth.bean.base.CommandBase;
import com.jieli.bluetooth.bean.base.VoiceMode;
import com.jieli.bluetooth.bean.command.custom.CustomCmd;
import com.jieli.bluetooth.bean.device.DevBroadcastMsg;
import com.jieli.bluetooth.bean.device.eq.EqInfo;
import com.jieli.bluetooth.bean.device.eq.EqPresetInfo;
import com.jieli.bluetooth.bean.parameter.CustomParam;
import com.jieli.bluetooth.bean.response.ADVInfoResponse;
import com.jieli.bluetooth.constant.AttrAndFunCode;
import com.jieli.bluetooth.constant.Command;
import com.jieli.bluetooth.constant.ErrorCode;
import com.jieli.bluetooth.constant.StateCode;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.bluetooth.RcspCommandCallback;
import com.jieli.bluetooth.interfaces.rcsp.callback.BTRcspEventCallback;
import com.jieli.bluetooth.interfaces.rcsp.callback.OnRcspActionCallback;
import com.jieli.bluetooth.utils.CommandBuilder;
import com.proove.ble.constant.CommonEqInfo;
import com.proove.ble.constant.EqConstant;
import com.proove.ble.constant.EqPreset;
import com.proove.ble.constant.protocol.AncMode;
import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.constant.protocol.UiAction;
import com.proove.ble.constant.protocol.UiFunction;
import com.proove.ble.constant.protocol.WorkMode;
import com.proove.ble.data.BatteryInfo;
import com.proove.ble.data.UiInfo;
import com.yscoco.lib.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙RCSP协议管理类
 * 负责处理设备连接、数据交互、状态回调等功能
 */
public class BTRcspManager {
    private static final String TAG = "BTRcspManager";
    private static volatile BTRcspManager instance;
    private final List<BTRcspCallback> callbacks = new ArrayList<>();
    private EqPresetInfo mEqPresetInfo;
    private List<VoiceMode> mVoiceModes;

    /**
     * RCSP事件回调处理
     * 处理设备连接状态、音频模式、电量信息、EQ设置等回调事件
     */
    private final BTRcspEventCallback btRcspEventCallback = new BTRcspEventCallback() {
        /**
         * 设备连接状态回调
         * @param device 蓝牙设备
         * @param status 连接状态码
         */
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            super.onConnection(device, status);
            for (BTRcspCallback callback : callbacks) {
                switch (status) {
                    case StateCode.CONNECTION_CONNECTED, StateCode.CONNECTION_OK ->
                            callback.onConnectSuccess(device);
                    case StateCode.CONNECTION_DISCONNECT, StateCode.CONNECTION_FAILED ->
                            callback.onConnectFail(device);
                }
            }
        }

        /**
         * 音频模式列表回调
         * @param device 蓝牙设备
         * @param voiceModes 支持的音频模式列表
         */
        @Override
        public void onVoiceModeList(BluetoothDevice device, List<VoiceMode> voiceModes) {
            super.onVoiceModeList(device, voiceModes);
            mVoiceModes = voiceModes;
            boolean isSupported = voiceModes != null && !voiceModes.isEmpty();
            for (BTRcspCallback callback : callbacks) {
                callback.onFeatureSupportUpdate(device, DeviceFeature.ANC_MODE, isSupported);
            }
            Log.i(TAG, "onVoiceModeList: " + new Gson().toJson(voiceModes));
        }

        /**
         * 当前ANC模式回调
         * @param device 蓝牙设备
         * @param voiceMode 当前音频模式
         */
        @Override
        public void onCurrentVoiceMode(BluetoothDevice device, VoiceMode voiceMode) {
            super.onCurrentVoiceMode(device, voiceMode);
            AncMode ancMode = AncMode.valueOf(voiceMode.getMode());
            Log.i(TAG, "onCurrentVoiceMode: " + new Gson().toJson(ancMode));
            for (BTRcspCallback callback : callbacks) {
                callback.onAncModeUpdate(device, ancMode);
            }
        }

        /**
         * 设备广播信息回调(包含电量信息)
         * @param device 蓝牙设备
         * @param broadcast 广播消息
         */
        @Override
        public void onDeviceBroadcast(BluetoothDevice device, DevBroadcastMsg broadcast) {
            super.onDeviceBroadcast(device, broadcast);
            BatteryInfo battery = new BatteryInfo();
            battery.setLeftBattery(broadcast.getLeftDeviceQuantity());
            battery.setRightBattery(broadcast.getRightDeviceQuantity());
            battery.setCaseBattery(broadcast.getChargingBinQuantity());

            Log.i(TAG, "获取设备电量: " + new Gson().toJson(battery));
            for (BTRcspCallback callback : callbacks) {
                callback.onBatteryInfoUpdate(device, battery);
            }
        }

        /**
         * EQ设置变化回调
         * @param device 蓝牙设备
         * @param eqInfo EQ信息
         */
        @Override
        public void onEqChange(BluetoothDevice device, EqInfo eqInfo) {
            super.onEqChange(device, eqInfo);
            CommonEqInfo tempCommonEqInfo = new CommonEqInfo();
            // 设置通用属性
            tempCommonEqInfo.setFreq(eqInfo.getFreqs());
            float[] gain = new float[eqInfo.getCount()];
            for (int i = 0; i < gain.length; i++) {
                gain[i] = eqInfo.getValue()[i];
            }
            tempCommonEqInfo.setGain(gain);

            // 处理不同类型的EQ模式
            if (eqInfo.getMode() < EqConstant.PRESET_MAX) {
                // 处理预设EQ
                handlePresetEq(tempCommonEqInfo, eqInfo.getMode());
            } else {
                // 处理自定义EQ
                handleCustomEq(tempCommonEqInfo, eqInfo.getMode());
            }

            // 通知回调
            for (BTRcspCallback callback : callbacks) {
                callback.onEqInfoUpdate(device, tempCommonEqInfo);
            }
        }

        /**
         * EQ预设信息变化回调
         * @param device 蓝牙设备
         * @param eqPresetInfo EQ预设信息
         */
        @Override
        public void onEqPresetChange(BluetoothDevice device, EqPresetInfo eqPresetInfo) {
            super.onEqPresetChange(device, eqPresetInfo);
            mEqPresetInfo = eqPresetInfo;
            boolean isSupported = eqPresetInfo != null && eqPresetInfo.getEqInfos() != null 
                    && !eqPresetInfo.getEqInfos().isEmpty();
            for (BTRcspCallback callback : callbacks) {
                callback.onFeatureSupportUpdate(device, DeviceFeature.EQ_SETTINGS, isSupported);
            }
            LogUtil.info(TAG, "EQ设置数据回调： " + new Gson().toJson(eqPresetInfo));
        }

        /**
         * 自定义命令回调
         * 处理设备返回的自定义命令响应
         * @param device 蓝牙设备
         * @param cmd 命令数据
         */
        @Override
        public void onDeviceCommand(BluetoothDevice device, CommandBase cmd) {
            super.onDeviceCommand(device, cmd);
            Log.i(TAG, "自定义消息回调: " + new Gson().toJson(cmd));
            if (cmd.getId() != Command.CMD_EXTRA_CUSTOM) {
                return;
            }
            CustomCmd customCmd = (CustomCmd) cmd;
            CustomParam param = customCmd.getParam();
            // 处理自定义命令响应
            if (param != null) {
                byte[] responseData = param.getData();
                for (BTRcspCallback callback : callbacks) {
                    callback.onCustomCmdResponse(device, responseData);
                }
            }
            // 需要回复的命令处理
            if (cmd.getType() == CommandBase.FLAG_HAVE_PARAMETER_AND_RESPONSE) {
                byte[] responseData = new byte[0];
                customCmd.setParam(new CustomParam(responseData));
                customCmd.setStatus(StateCode.STATUS_SUCCESS);
                RCSPController.getInstance().sendRcspResponse(device, customCmd);
            }
        }
    };

    private BTRcspManager() {
        RCSPController.getInstance().addBTRcspEventCallback(btRcspEventCallback);
    }

    public static BTRcspManager getInstance() {
        if (instance == null) {
            synchronized (BTRcspManager.class) {
                if (instance == null) {
                    instance = new BTRcspManager();
                }
            }
        }
        return instance;
    }

    /**
     * 注册回调接口
     *
     * @param callback 需要注册的回调接口
     */
    public void registerCallback(BTRcspCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    /**
     * 获取EQ预设信息
     * 从设备获取当前的EQ设置信息
     */
    public void getEqPreset() {
        RCSPController.getInstance().getEqInfo(RCSPController.getInstance().getUsingDevice(), new OnRcspActionCallback<Boolean>() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevice, Boolean aBoolean) {
                Log.i(TAG, "onSuccess: ");
            }

            @Override
            public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                Log.i(TAG, "onError: " + new Gson().toJson(baseError));
            }
        });
    }

    /**
     * 设置EQ预设
     * 向设备发送EQ设置命令
     *
     * @param eqPreset 要设置的EQ预设
     */
    public void setEqPreset(EqPreset eqPreset) {
        if (mEqPresetInfo == null || mEqPresetInfo.getEqInfos() == null || mEqPresetInfo.getEqInfos().isEmpty()) {
            return;
        }
        List<EqInfo> eqInfoList = mEqPresetInfo.getEqInfos();
        EqInfo targetEqInfo = null;
        for (EqInfo eqInfo : eqInfoList) {
            if (eqInfo.getMode() == eqPreset.getIndex()) {
                targetEqInfo = eqInfo;
            }
        }
        if (targetEqInfo == null) {
            return;
        }
        int mode = eqPreset.getIndex();
        targetEqInfo.setMode(mode >= EqConstant.PRESET_MAX ? 0 : mode);
        RCSPController.getInstance().configEqInfo(RCSPController.getInstance().getUsingDevice(), targetEqInfo, new OnRcspActionCallback<>() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevice, Boolean aBoolean) {
                LogUtil.info(TAG, "configEqInfo onSuccess");
            }

            @Override
            public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                LogUtil.info(TAG, "configEqInfo onError");
            }
        });
    }

    public void unregisterCallback(BTRcspCallback callback) {
        callbacks.remove(callback);
    }

    /**
     * 获取设备按键设置
     * 获取耳机按键的功能映射信息
     */
    public void getKeyData() {
        RCSPController.getInstance().getDeviceSettingsInfo(RCSPController.getInstance().getUsingDevice(),
                0x01 << AttrAndFunCode.ADV_TYPE_KEY_SETTINGS, new OnRcspActionCallback<>() {
                    @Override
                    public void onSuccess(BluetoothDevice bluetoothDevice, ADVInfoResponse advInfoResponse) {
                        Log.i(TAG, "getKeyData onSuccess: " + new Gson().toJson(advInfoResponse));
                        dealUIInfo(advInfoResponse);
                    }

                    @Override
                    public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                        Log.i(TAG, "getKeyData onError: " + new Gson().toJson(baseError));
                    }
                });
    }

    /**
     * 获取ANC模式
     * 获取设备当前的降噪模式和支持的降噪模式列表
     */
    public void getAncMode() {
        RCSPController.getInstance().getAllVoiceModes(RCSPController.getInstance().getUsingDevice(), new OnRcspActionCallback<>() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevice, Boolean aBoolean) {
                LogUtil.info(TAG, "getAllVoiceModes onSuccess");
            }

            @Override
            public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                LogUtil.error(TAG, "getAllVoiceModes onError " + new Gson().toJson(baseError));
            }
        });
        RCSPController.getInstance().getCurrentVoiceMode(RCSPController.getInstance().getUsingDevice(), new OnRcspActionCallback<>() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevice, Boolean aBoolean) {
                LogUtil.info(TAG, "getCurrentVoiceMode onSuccess");
            }

            @Override
            public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                LogUtil.error(TAG, "getCurrentVoiceMode onError " + new Gson().toJson(baseError));
            }
        });
    }

    /**
     * 获取工作模式
     * 获取设备当前的工作模式(如音乐模式、通话模式等)
     */
    public void getWorkMode() {
        RCSPController.getInstance().getDeviceSettingsInfo(
                RCSPController.getInstance().getUsingDevice(),
                0x01 << AttrAndFunCode.ADV_TYPE_WORK_MODE,
                new OnRcspActionCallback<>() {
                    @Override
                    public void onSuccess(BluetoothDevice bluetoothDevice, ADVInfoResponse advInfoResponse) {
                        boolean isSupported = advInfoResponse != null && advInfoResponse.getWorkModel() != 0;
                        for (BTRcspCallback callback : callbacks) {
                            callback.onFeatureSupportUpdate(bluetoothDevice, DeviceFeature.WORK_MODE, isSupported);
                        }
                        
                        if (isSupported) {
                            WorkMode workMode = WorkMode.valueOf(advInfoResponse.getWorkModel());
                            LogUtil.info(TAG, "getWorkMode = " + new Gson().toJson(workMode));
                            for (BTRcspCallback callback : callbacks) {
                                callback.onWorkModeUpdate(bluetoothDevice, workMode);
                            }
                        }
                    }

                    @Override
                    public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                        LogUtil.info(TAG, "getWorkMode error = " + new Gson().toJson(baseError));
                        notifyError(bluetoothDevice, baseError.getCode(), baseError.getMessage());
                        // 发生错误时，认为不支持该功能
                        for (BTRcspCallback callback : callbacks) {
                            callback.onFeatureSupportUpdate(bluetoothDevice, DeviceFeature.WORK_MODE, false);
                        }
                    }
                });
    }

    /**
     * 设置ANC模式
     * 设置设备的降噪模式
     *
     * @param mode 要设置的ANC模式
     */
    public void setAncMode(AncMode mode) {
        VoiceMode targetVoiceMode = null;
        if (mVoiceModes == null) {
            Log.i(TAG, "setAncMode: 不支持该功能");
            return;
        }
        for (VoiceMode voiceMode : mVoiceModes) {
            if (voiceMode.getMode() == mode.getValue()) {
                targetVoiceMode = voiceMode;
            }
        }
        if (targetVoiceMode == null) {
            return;
        }
        RCSPController.getInstance().setCurrentVoiceMode(RCSPController.getInstance().getUsingDevice(), targetVoiceMode, new OnRcspActionCallback<Boolean>() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevice, Boolean aBoolean) {
                LogUtil.info(TAG, "setCurrentVoiceMode onSuccess");
            }

            @Override
            public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                LogUtil.error(TAG, "setCurrentVoiceMode onError " + baseError);
            }
        });
    }

    /**
     * 处理UI信息
     * 解析并处理设备返回的按键设置信息
     *
     * @param advInfoResponse 设备返回的信息
     */
    private void dealUIInfo(ADVInfoResponse advInfoResponse) {
        boolean isSupported = false;
        UiInfo uiInfo = new UiInfo();
        
        if (advInfoResponse != null) {
            List<ADVInfoResponse.KeySettings> keySettingsList = advInfoResponse.getKeySettingsList();
            if (keySettingsList != null && !keySettingsList.isEmpty()) {
                isSupported = true;
                for (ADVInfoResponse.KeySettings keySettings : keySettingsList) {
                    DeviceSide deviceSide = DeviceSide.valueOf(keySettings.getKeyNum());
                    UiAction uiAction = UiAction.valueOf(keySettings.getAction());
                    UiFunction uiFunction = UiFunction.valueOf(keySettings.getFunction());
                    switch (deviceSide) {
                        case LEFT -> {
                            switch (uiAction) {
                                case CLICK -> uiInfo.setLeftOneClickFunction(uiFunction);
                                case DOUBLE_CLICK -> uiInfo.setLeftTwoClickFunction(uiFunction);
                                case TRIPLE_HIT -> uiInfo.setLeftThreeClickFunction(uiFunction);
                                case LONG_PRESS -> uiInfo.setLeftLongClickFunction(uiFunction);
                            }
                        }
                        case RIGHT -> {
                            switch (uiAction) {
                                case CLICK -> uiInfo.setRightOneClickFunction(uiFunction);
                                case DOUBLE_CLICK -> uiInfo.setRightTwoClickFunction(uiFunction);
                                case TRIPLE_HIT -> uiInfo.setRightThreeClickFunction(uiFunction);
                                case LONG_PRESS -> uiInfo.setRightLongClickFunction(uiFunction);
                            }
                        }
                    }
                }
            }
        }

        // 通知按键设置功能支持状态
        for (BTRcspCallback callback : callbacks) {
            callback.onFeatureSupportUpdate(RCSPController.getInstance().getUsingDevice(), 
                    DeviceFeature.KEY_SETTINGS, isSupported);
        }

        if (isSupported) {
            // 如果支持按键设置，才发送按键信息更新
            for (BTRcspCallback callback : callbacks) {
                callback.onKeySettingsUpdate(RCSPController.getInstance().getUsingDevice(), uiInfo);
            }
        }
    }

    /**
     * 发送自定义命令
     * 向设备发送自定义的命令数据
     *
     * @param cmdData 命令数据
     */
    public void sendCustomCmd(byte[] cmdData) {
        CommandBase customCmd = CommandBuilder.buildCustomCmd(cmdData);
        RCSPController.getInstance().sendRcspCommand(
                RCSPController.getInstance().getUsingDevice(),
                customCmd,
                new RcspCommandCallback() {
                    @Override
                    public void onCommandResponse(BluetoothDevice device, CommandBase cmd) {
                        if (cmd.getStatus() != StateCode.STATUS_SUCCESS) {
                            notifyError(device, ErrorCode.SUB_ERR_RESPONSE_BAD_STATUS,
                                    "Device response an bad status : " + cmd.getStatus());
                            return;
                        }

                        CustomCmd customCmd = (CustomCmd) cmd;
                        byte[] responseData = new byte[0];
                        customCmd.setParam(new CustomParam(responseData));
                        customCmd.setStatus(StateCode.STATUS_SUCCESS);
                        RCSPController.getInstance().sendRcspResponse(device, customCmd);

                        // 通知自定义命令响应
                        for (BTRcspCallback callback : callbacks) {
                            callback.onCustomCmdResponse(device, responseData);
                        }
                    }

                    @Override
                    public void onErrCode(BluetoothDevice device, BaseError error) {
                        Log.i(TAG, "onErrCode: " + new Gson().toJson(error));
                        notifyError(device, error.getCode(), error.getMessage());
                    }
                });
    }

    /**
     * 释放资源
     * 清除回调、释放实例等清理工作
     */
    public void release() {
        callbacks.clear();
        RCSPController.getInstance().removeBTRcspEventCallback(btRcspEventCallback);
        instance = null;
    }

    /**
     * RCSP回调接口
     * 定义了所有可能的回调事件
     */
    public interface BTRcspCallback {
        /**
         * 连接成功回调
         *
         * @param device 已连接的蓝牙设备
         */
        default void onConnectSuccess(BluetoothDevice device) {
        }

        /**
         * 连接失败回调
         *
         * @param device 连接失败的蓝牙设备
         */
        default void onConnectFail(BluetoothDevice device) {
        }

        /**
         * 电池信息更新回调
         *
         * @param device      蓝牙设备
         * @param batteryInfo 电池信息
         */
        default void onBatteryInfoUpdate(BluetoothDevice device, BatteryInfo batteryInfo) {
        }

        /**
         * EQ信息更新回调
         *
         * @param device 蓝牙设备
         * @param eqInfo EQ信息
         */
        default void onEqInfoUpdate(BluetoothDevice device, CommonEqInfo eqInfo) {
        }

        /**
         * ANC模式更新回调
         *
         * @param device  蓝牙设备
         * @param ancMode 当前ANC模式
         */
        default void onAncModeUpdate(BluetoothDevice device, AncMode ancMode) {
        }

        /**
         * 工作模式更新回调
         *
         * @param device   蓝牙设备
         * @param workMode 当前工作模式
         */
        default void onWorkModeUpdate(BluetoothDevice device, WorkMode workMode) {
        }

        /**
         * 自定义命令响应回调
         *
         * @param device       蓝牙设备
         * @param responseData 响应数据
         */
        default void onCustomCmdResponse(BluetoothDevice device, byte[] responseData) {
        }

        /**
         * 按键设置信息更新回调
         *
         * @param device 蓝牙设备
         * @param uiInfo 按键功能映射信息
         */
        default void onKeySettingsUpdate(BluetoothDevice device, UiInfo uiInfo) {
        }

        /**
         * 错误回调
         *
         * @param device    蓝牙设备
         * @param errorCode 错误码
         * @param errorMsg  错误信息
         */
        default void onError(BluetoothDevice device, int errorCode, String errorMsg) {
        }

        /**
         * 功能支持状态回调
         * @param device 蓝牙设备
         * @param feature 功能类型
         * @param isSupported 是否支持
         */
        default void onFeatureSupportUpdate(BluetoothDevice device, DeviceFeature feature, boolean isSupported) {}
    }

    /**
     * 处理预设EQ
     * 设置预设EQ的相关参数
     *
     * @param eqInfo EQ信息对象
     * @param mode   EQ模式
     */
    private void handlePresetEq(CommonEqInfo eqInfo, int mode) {
        EqPreset eqPreset = EqPreset.valueOf(mode);
        eqInfo.setIndex(eqPreset.getIndex());
        String eqName = "无";
        switch (eqPreset.getIndex()) {
            case 0 -> eqName = "默认";
            case 1 -> eqName = "流行";
            case 2 -> eqName = "乡村";
            case 3 -> eqName = "爵士";
            case 4 -> eqName = "慢歌";
            case 5 -> eqName = "古典";
        }
        eqInfo.setEqName(eqName);
    }

    /**
     * 处理自定义EQ
     * 设置自定义EQ的相关参数
     *
     * @param eqInfo EQ信息对象
     * @param mode   EQ模式
     */
    private void handleCustomEq(CommonEqInfo eqInfo, int mode) {
        switch (mode) {
            case EqConstant.CUSTOM_INDEX_1 -> {
                eqInfo.setIndex(EqConstant.CUSTOM_INDEX_1);
                eqInfo.setEqName("Custom Sound 1");
            }
            case EqConstant.CUSTOM_INDEX_2 -> {
                eqInfo.setIndex(EqConstant.CUSTOM_INDEX_2);
                eqInfo.setEqName("Custom Sound 2");
            }
            case EqConstant.CUSTOM_INDEX_3 -> {
                eqInfo.setIndex(EqConstant.CUSTOM_INDEX_3);
                eqInfo.setEqName("Custom Sound 3");
            }
            // 可以继续添加更多自定义EQ
            default -> {
                eqInfo.setIndex(mode);
                eqInfo.setEqName("Custom Sound " + (mode - EqConstant.PRESET_MAX + 1));
            }
        }
    }

    /**
     * 统一错误通知
     * 向所有注册的回调发送错误信息
     *
     * @param device    蓝牙设备
     * @param errorCode 错误码
     * @param errorMsg  错误信息
     */
    private void notifyError(BluetoothDevice device, int errorCode, String errorMsg) {
        for (BTRcspCallback callback : callbacks) {
            callback.onError(device, errorCode, errorMsg);
        }
    }

    /**
     * 设备功能枚举
     * 定义了设备可能支持的所有功能类型
     */
    public enum DeviceFeature {
        /**
         * 降噪模式功能
         */
        ANC_MODE,
        
        /**
         * EQ均衡器设置功能
         */
        EQ_SETTINGS,
        
        /**
         * 按键自定义功能
         */
        KEY_SETTINGS,
        
        /**
         * 工作模式切换功能
         */
        WORK_MODE,
        
        /**
         * 电量显示功能
         */
        BATTERY_INFO,
        
        /**
         * 自定义命令功能
         */
        CUSTOM_COMMAND
    }
} 