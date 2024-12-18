package com.proove.smart.model;

import static com.jieli.bluetooth.constant.StateCode.CONNECTION_CONNECTED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_DISCONNECT;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_FAILED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_OK;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import androidx.room.Room;

import com.jieli.bluetooth.bean.base.BaseError;
import com.jieli.bluetooth.bean.base.CommandBase;
import com.jieli.bluetooth.bean.base.VoiceMode;
import com.jieli.bluetooth.bean.command.custom.CustomCmd;
import com.jieli.bluetooth.bean.device.DevBroadcastMsg;
import com.jieli.bluetooth.bean.device.music.ID3MusicInfo;
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
import com.jieli.bluetooth.tool.DeviceAddrManager;
import com.jieli.bluetooth.utils.CommandBuilder;
import com.proove.ble.constant.BatteryType;
import com.proove.ble.constant.Constant;
import com.proove.ble.constant.DeviceInfoType;
import com.proove.ble.constant.EqConstant;
import com.proove.ble.constant.Product;
import com.proove.ble.constant.SpConstant;
import com.proove.ble.constant.protocol.AncMode;
import com.proove.ble.constant.protocol.HearingMode;
import com.proove.ble.constant.protocol.LightsMode;
import com.proove.ble.constant.protocol.WorkMode;
import com.proove.ble.data.BatteryInfo;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.entity.EqInfoEntity;
import com.proove.smart.R;
import com.proove.smart.database.AppDatabase;
import com.proove.smart.manager.BleManager;
import com.proove.smart.manager.BluetoothManager;
import com.proove.smart.manager.DeviceManager;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.ByteUtil;
import com.yscoco.lib.util.ContextUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.StringUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceEarModelImpl implements DeviceEarModel {
    private static final String TAG = "DeviceModelImpl";
    private List<VoiceMode> mVoiceModes;
    private final RCSPController controller = RCSPController.getInstance();
    private final VolumeChangeManager volumeChangeManager;

    private AppDatabase database;
    private Handler handler;
    private final HandlerThread handlerThread = new HandlerThread(TAG);
    private final BTRcspEventCallback btRcspEventCallback = new BTRcspEventCallback() {
        @Override
        public void onDeviceBroadcast(BluetoothDevice device, DevBroadcastMsg broadcast) {
            super.onDeviceBroadcast(device, broadcast);

            BatteryInfo battery = new BatteryInfo();
            battery.setLeftBattery(broadcast.getLeftDeviceQuantity());
            battery.setRightBattery(broadcast.getRightDeviceQuantity());
            battery.setCaseBattery(broadcast.getChargingBinQuantity());
            Product product = DeviceManager.getInstance().getCurrentProduct();
            if (product != null && product.getBatteryType() == BatteryType.SINGLE) {
                battery.setCaseBattery(broadcast.getLeftDeviceQuantity());
            }
            notifyBatteryInfo(battery);
        }

        @Override
        public void onCurrentVoiceMode(BluetoothDevice device, VoiceMode voiceMode) {
            LogUtil.info(TAG, "onCurrentVoiceMode " + voiceMode);
            notifyAncMode(AncMode.valueOf(voiceMode.getMode()));
        }

        @Override
        public void onVoiceModeList(BluetoothDevice device, List<VoiceMode> voiceModes) {
            super.onVoiceModeList(device, voiceModes);
            mVoiceModes = voiceModes;
            LogUtil.info(TAG, "VoiceModeList = " + mVoiceModes);
        }

        @Override
        public void onConnection(BluetoothDevice device, int status) {
            super.onConnection(device, status);
            LogUtil.info(TAG, "BTRcspEventCallback onConnection mac = "
                    + device.getAddress() + " status = " + status);

            switch (status) {
                case CONNECTION_CONNECTED, CONNECTION_OK -> {
                    boolean equals = TextUtils.equals(device.getAddress(), DeviceManager.getInstance().getCurrentMac());
                    macConnected = device.getAddress();
                    notifyConnectState(equals);
                }
                case CONNECTION_DISCONNECT, CONNECTION_FAILED -> {
                    DeviceManager.getInstance().setCurrentMac(device.getAddress());
                    notifyConnectState(false);

                }
            }
        }

        @Override
        public void onID3MusicInfo(BluetoothDevice device, ID3MusicInfo id3MusicInfo) {
            super.onID3MusicInfo(device, id3MusicInfo);
            notifyPlayState(id3MusicInfo.isPlayStatus());
        }

        @Override
        public void onDeviceCommand(BluetoothDevice device, CommandBase cmd) {
            super.onDeviceCommand(device, cmd);
            //此处将回调设备发送的命令
            Log.i(TAG, "onDeviceCommand: " + cmd.getId());
            if (cmd.getId() == Command.CMD_EXTRA_CUSTOM) { //只处理自定义命令数据

                CustomCmd customCmd = (CustomCmd) cmd;
                CustomParam param = customCmd.getParam();
                if (null == param) {
                    if (cmd.getType() == CommandBase.FLAG_HAVE_PARAMETER_AND_RESPONSE) { //需要回复
                        byte[] responseData = new byte[0]; //可以设置回复的数据
                        customCmd.setParam(new CustomParam(responseData));
                        customCmd.setStatus(StateCode.STATUS_SUCCESS);
                        controller.sendRcspResponse(device, customCmd); //发送命令回复
                    }
                    return;
                }
                byte[] data = param.getData(); //自定义数据
                LogUtil.info(TAG, String.format("deviceMac = %s onDeviceCommand data = %s",
                        device.getAddress(), ByteUtil.bytesToHexString(data)));
                if (cmd.getType() == CommandBase.FLAG_HAVE_PARAMETER_AND_RESPONSE) { //需要回复
                    byte[] responseData = new byte[0]; //可以设置回复的数据
                    customCmd.setParam(new CustomParam(responseData));
                    customCmd.setStatus(StateCode.STATUS_SUCCESS);
                    controller.sendRcspResponse(device, customCmd); //发送命令回复
                }
                dealCustomData(data);
            } else if (cmd.getId() == Command.CMD_ADV_DEV_REQUEST_OPERATION) {
                getWorkMode();
            }
        }
    };

    private final VolumeChangeManager.VolumeChangeListener volumeChangeListener = volume -> notifyVolume();
    private final DeviceManager.IDeviceEventListener deviceEventListener = new DeviceManager.IDeviceEventListener() {
        @Override
        public void onDeviceInfoChange(DeviceInfoType type, DeviceInfo deviceInfo) {
            notifyDeviceName(deviceInfo.getDeviceName());
        }

        @Override
        public void onConnectionChange(DeviceInfo device, int state) {
            if (device.getProductId() == Product.J50.getProductId()) {
                notifyDeviceName("J50");
            } else if (device.getProductId() == Product.A3Pro.getProductId()) {
                notifyDeviceName("A3Pro");
            }
        }

        @Override
        public void onDeviceListChange(List<DeviceInfo> deviceInfoList) {

        }
    };

    private IDataCallback<Boolean> connectStateCallback;
    private IDataCallback<String> deviceNameCallback;
    private IDataCallback<BatteryInfo> batteryCallback;
    private IDataCallback<AncMode> ancModeCallback;
    private IDataCallback<WorkMode> workModeCallback;

    private IDataCallback<byte[]> timingCallback;

    private IDataCallback<Boolean> is3DCallback;
    private IDataCallback<LightsMode> lightsModeCallback;
    private IDataCallback<HearingMode> hearingModeCallback;
    private IDataCallback<Integer> volumeCallback;
    private IDataCallback<Boolean> playStateCallback;

    private IDataCallback<Boolean> rebootDeviceCallback;

    public DeviceEarModelImpl() {
        controller.addBTRcspEventCallback(btRcspEventCallback);
        volumeChangeManager = new VolumeChangeManager(ContextUtil.getAppContext());
        volumeChangeManager.registerReceiver();
        volumeChangeManager.setVolumeChangeListener(volumeChangeListener);
        DeviceManager.getInstance().addDeviceEventListener(deviceEventListener);

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {
            database = Room
                    .databaseBuilder(ContextUtil.getAppContext(), AppDatabase.class, Constant.APP_DATABASE_NAME)
                    .build();
        });
    }

    @Override
    public void onClean() {
        controller.removeBTRcspEventCallback(btRcspEventCallback);
        volumeChangeManager.unregisterReceiver();
        volumeChangeManager.setVolumeChangeListener(null);
        database.close();
        handlerThread.quitSafely();
        DeviceManager.getInstance().removeDeviceEventListener(deviceEventListener);
    }

    private String macConnected;

    @Override
    public void setConnectMac(String mac) {
        boolean isConnected = controller.isDeviceConnected(BluetoothUtil
                .getBluetoothDevice(DeviceManager.getInstance().getCurrentMac()));
        macConnected = mac;
        LogUtil.info(TAG, "setConnectMac = " + isConnected);
        notifyConnectState(isConnected);
    }

    @Override
    public void setConnectStatusListener(IDataCallback<Boolean> callback) {
        connectStateCallback = callback;
    }

    @Override
    public void getDeviceName() {
        DeviceInfo deviceInfo = DeviceManager.getInstance().getCurrentDeviceInfo();
        if (deviceInfo == null) {
            return;
        }
        notifyDeviceName(deviceInfo.getDeviceName());
    }

    @Override
    public void setDeviceName(String name, IDataCallback<Boolean> callback) {
        if (callback == null) {
            return;
        }
        if (StringUtil.isNullOrEmpty(name) && name.getBytes().length <= Constant.DEVICE_NAME_LIMIT) {
            callback.onResult(false);
            return;
        }
        controller.configDeviceName(controller.getUsingDevice(), name, new OnRcspActionCallback<>() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevice, Integer integer) {
                LogUtil.info(TAG, "configDeviceName onSuccess = " + integer);
                callback.onResult(true);
                DeviceInfo deviceInfo = DeviceManager.getInstance().getCurrentDeviceInfo();
                if (deviceInfo != null) {
                    DeviceManager.getInstance().saveDeviceName(deviceInfo.getMac(), name);
                }
            }

            @Override
            public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                callback.onResult(false);
            }
        });
    }

    @Override
    public void setDeviceNameListener(IDataCallback<String> callback) {
        deviceNameCallback = callback;
    }

    @Override
    public void getBattery() {
        ADVInfoResponse advInfoResponse = controller
                .getADVInfo(RCSPController.getInstance().getUsingDevice());
        if (advInfoResponse == null) {
            return;
        }
        BatteryInfo battery = new BatteryInfo();
        battery.setLeftBattery(advInfoResponse.getLeftDeviceQuantity());
        battery.setRightBattery(advInfoResponse.getRightDeviceQuantity());
        battery.setCaseBattery(advInfoResponse.getChargingBinQuantity());
        Product product = DeviceManager.getInstance().getCurrentProduct();
        if (product != null && product.getBatteryType() == BatteryType.SINGLE) {
            battery.setCaseBattery(advInfoResponse.getLeftDeviceQuantity());
        }
        notifyBatteryInfo(battery);
    }

    @Override
    public void setBatteryListener(IDataCallback<BatteryInfo> callback) {
        batteryCallback = callback;
    }

    @Override
    public void getAncMode() {
        controller.getAllVoiceModes(controller.getUsingDevice(), new OnRcspActionCallback<>() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevice, Boolean aBoolean) {
                LogUtil.info(TAG, "getAllVoiceModes onSuccess");
            }

            @Override
            public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                LogUtil.error(TAG, "getAllVoiceModes onError " + baseError);
            }
        });
        controller.getCurrentVoiceMode(controller.getUsingDevice(), new OnRcspActionCallback<>() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevice, Boolean aBoolean) {
                LogUtil.info(TAG, "getCurrentVoiceMode onSuccess");
            }

            @Override
            public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                LogUtil.error(TAG, "getCurrentVoiceMode onError " + baseError);
            }
        });
    }

    @Override
    public void get3DMode() {
        sendCustomCmd(new byte[]{(byte) 0x02, (byte) 0xFF, (byte) 0xFF});
    }

    @Override
    public void set3DMode(boolean is3D) {
        sendCustomCmd(new byte[]{(byte) 0x02, (byte) 0xFF, is3D ? (byte) 0x01 : (byte) 0x00});
    }

    @Override
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
        controller.setCurrentVoiceMode(controller.getUsingDevice(), targetVoiceMode, new OnRcspActionCallback<Boolean>() {
            @Override
            public void onSuccess(BluetoothDevice bluetoothDevice, Boolean aBoolean) {
                LogUtil.info(TAG, "setCurrentVoiceMode onSuccess");
                notifyAncMode(mode);
            }

            @Override
            public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                LogUtil.error(TAG, "setCurrentVoiceMode onError " + baseError);
            }
        });
    }

    @Override
    public void setAncModeListener(IDataCallback<AncMode> callback) {
        ancModeCallback = callback;

    }

    @Override
    public void getWorkMode() {
        controller.getDeviceSettingsInfo(controller.getUsingDevice(),
                0x01 << AttrAndFunCode.ADV_TYPE_WORK_MODE, new OnRcspActionCallback<>() {
                    @Override
                    public void onSuccess(BluetoothDevice bluetoothDevice, ADVInfoResponse advInfoResponse) {
                        int workModel = advInfoResponse.getWorkModel();
                        LogUtil.info(TAG, "getWorkMode = " + workModel);
                        notifyWorkMode(WorkMode.valueOf(workModel));
                    }

                    @Override
                    public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {

                    }
                });
    }

    @Override
    public void getLightsMode() {
        sendCustomCmd(new byte[]{(byte) 0x01, (byte) 0xFF});
    }

    @Override
    public void getTimingMode() {
        sendCustomCmd(new byte[]{(byte) 0x02, (byte) 0xFF});
    }

    public void getDeviceResetMode() {
        Product currentProduct = DeviceManager.getInstance().getCurrentProduct();
        if (currentProduct != null) {
            if (currentProduct.getProductId() == 0x0003 || currentProduct.getProductId() == 0x000C) {
                sendCustomCmd(new byte[]{(byte) 0x03, (byte) 0x01});
                return;
            }
        }
        sendCustomCmd(new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0x00});
    }

    @Override
    public void getHearingMode() {
        sendCustomCmd(new byte[]{(byte) 0x04, (byte) 0xFF});
    }

    @Override
    public void setLightsMode(LightsMode lightsMode) {
        sendCustomCmd(new byte[]{(byte) 0x01, (byte) lightsMode.getValue()});
    }

    @Override
    public void setHearingModeListener(IDataCallback<HearingMode> callback) {
        this.hearingModeCallback = callback;
    }

    @Override
    public void setHearingMode(HearingMode hearingMode) {
        sendCustomCmd(new byte[]{(byte) 0x04, (byte) hearingMode.getValue()});
    }

    @Override
    public void setLightsModeListener(IDataCallback<LightsMode> callback) {
        lightsModeCallback = callback;
    }

    @Override
    public void rebootDevice() {
        controller.rebootDevice(controller.getUsingDevice(), new OnRcspActionCallback<Boolean>() {
            @Override
            public void onSuccess(BluetoothDevice device, Boolean message) {
                LogUtil.info(TAG, "rebootDevice onSuccess");
                notifyRebootDevice(true);
            }

            @Override
            public void onError(BluetoothDevice device, BaseError error) {
                LogUtil.info(TAG, "rebootDevice onError");
                notifyRebootDevice(false);
            }
        });
    }

    @Override
    public void resetEqData() {
        String mac = DeviceManager.getInstance().getCurrentMac();
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return;
        }
        handler.post(() -> {
            EqInfoEntity eqInfo1 = database.eqInfoDao().getEqInfo(mac, EqConstant.CUSTOM_INDEX_1);
            EqInfoEntity eqInfo2 = database.eqInfoDao().getEqInfo(mac, EqConstant.CUSTOM_INDEX_2);
            if (eqInfo1 != null) {
                database.eqInfoDao().removeEqInfo(eqInfo1);
            }
            if (eqInfo2 != null) {
                database.eqInfoDao().removeEqInfo(eqInfo2);
            }
        });
    }

    @Override
    public void setWorkMode(WorkMode workMode) {
        controller.modifyDeviceSettingsInfo(controller.getUsingDevice(),
                AttrAndFunCode.ADV_TYPE_WORK_MODE, new byte[]{(byte) workMode.getValue()},
                new OnRcspActionCallback<>() {
                    @Override
                    public void onSuccess(BluetoothDevice bluetoothDevice, Integer integer) {
                        LogUtil.info(TAG, "setWorkMode onSuccess");
                        notifyWorkMode(workMode);
                    }

                    @Override
                    public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                        LogUtil.info(TAG, "setWorkMode onError " + baseError);
                    }
                });
    }

    @Override
    public void setWorkModeListener(IDataCallback<WorkMode> callback) {
        workModeCallback = callback;
    }

    @Override
    public void setTimingModeListener(IDataCallback<byte[]> callback) {
        timingCallback = callback;
    }

    @Override
    public void setIs3DCallback(IDataCallback<Boolean> is3DCallback) {
        this.is3DCallback = is3DCallback;
    }

    @Override
    public void setTimingMode(byte[] timingMode) {
        sendCustomCmd(timingMode);
    }

    @Override
    public void getVolume() {
        notifyVolume();
    }

    @Override
    public void setVolume(int volume) {
        volumeChangeManager.setVolumePercent(volume);
    }

    @Override
    public void setVolumeListener(IDataCallback<Integer> callback) {
        volumeCallback = callback;
    }

    @Override
    public void playOrPause() {
        LogUtil.info(TAG, "musicPlayOrPause");
        controller.iD3MusicPlayOrPause(controller.getUsingDevice(), new OnRcspActionCallback<Boolean>() {
            @Override
            public void onSuccess(BluetoothDevice device, Boolean message) {
                //成功回调
            }

            @Override
            public void onError(BluetoothDevice device, BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });
    }

    @Override
    public void previousSong() {
        controller.iD3MusicPlayPrev(controller.getUsingDevice(), new OnRcspActionCallback<Boolean>() {
            @Override
            public void onSuccess(BluetoothDevice device, Boolean message) {
                //成功回调
            }

            @Override
            public void onError(BluetoothDevice device, BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });
    }

    @Override
    public void nextSong() {
        controller.iD3MusicPlayNext(controller.getUsingDevice(), new OnRcspActionCallback<Boolean>() {
            @Override
            public void onSuccess(BluetoothDevice device, Boolean message) {
                LogUtil.info(TAG, "nextSong " + message);
                //成功回调
            }

            @Override
            public void onError(BluetoothDevice device, BaseError error) {
                //失败回调
                //error - 错误信息
                LogUtil.info(TAG, "nextSong " + error);
            }
        });
    }

    @Override
    public void getPlayState() {
        controller.getID3MusicInfo(controller.getUsingDevice(), new OnRcspActionCallback<Boolean>() {
            @Override
            public void onSuccess(BluetoothDevice device, Boolean message) {
                //成功回调
                //结果将会在BTRcspEventCallback#onMusicStatusChange回调
            }

            @Override
            public void onError(BluetoothDevice device, BaseError error) {
                //失败回调
                //error - 错误信息
            }
        });
    }

    @Override
    public void setPlayStateListener(IDataCallback<Boolean> callback) {
        playStateCallback = callback;
    }

    @Override
    public boolean isDeviceCall() {
        return controller.getDeviceInfo() != null && controller.getDeviceInfo().getPhoneStatus() == 1;
    }

    @Override
    public boolean isMusicActive() {
        return volumeChangeManager.isMusicActive();
    }

    @Override
    public void deleteDevice(String mac) {
        DeviceManager.getInstance().removeDevice(mac);
        RCSPController.getInstance().disconnectDevice(BluetoothUtil.getBluetoothDevice(mac));
        DeviceAddrManager.getInstance().removeHistoryBluetoothDevice(mac);
        removeHistoryBluetoothDevice(mac);
    }

    // 从历史记录中移除蓝牙设备，并解绑设备
    public void removeHistoryBluetoothDevice(String mac) {
        // 获取 BluetoothAdapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e("DeviceAddrManager", "Bluetooth is not supported on this device.");
            return;
        }

        // 获取设备对象
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);

        if (device == null) {
            Log.e("DeviceAddrManager", "Bluetooth device not found for MAC address: " + mac);
            return;
        }

        // 取消配对（解绑设备）
        try {
            // 从系统蓝牙设置中解除配对
            Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
            removeBondMethod.invoke(device);

            Log.i("DeviceAddrManager", "Successfully unpaired Bluetooth device with MAC: " + mac);

        } catch (Exception e) {
            Log.e("DeviceAddrManager", "Error while unpairing device: " + e.getMessage());
        }
    }
    @Override
    public void setRebootDeviceListener(IDataCallback<Boolean> callback) {
        rebootDeviceCallback = callback;
    }

    private void notifyRebootDevice(boolean isConnected) {
        if (rebootDeviceCallback == null) {
            return;
        }
        rebootDeviceCallback.onResult(isConnected);
    }

    private void notifyConnectState(boolean isConnected) {
        if (connectStateCallback == null) {
            return;
        }
        LogUtil.info(TAG, "notifyConnectState " + isConnected);
        connectStateCallback.onResult(isConnected);
    }

    private void notifyDeviceName(String name) {
        if (deviceNameCallback == null) {
            return;
        }
        deviceNameCallback.onResult(name);
    }

    private void notifyBatteryInfo(BatteryInfo batteryInfo) {
        //LogUtil.info(TAG, "notifyBatteryInfo = " + batteryInfo);
        if (batteryCallback == null) {
            return;
        }
        if (macConnected != null && DeviceManager.getInstance().getCurrentMac() != null) {
            boolean equals = TextUtils.equals(macConnected, DeviceManager.getInstance().getCurrentMac());
            if (!equals) {
                return;
            }
            batteryCallback.onResult(batteryInfo);
        }
    }

    private void notifyAncMode(AncMode ancMode) {
        if (ancModeCallback == null) {
            return;
        }
        if (macConnected != null && DeviceManager.getInstance().getCurrentMac() != null) {
            boolean equals = TextUtils.equals(macConnected, DeviceManager.getInstance().getCurrentMac());
            if (!equals) {
                return;
            }
            ancModeCallback.onResult(ancMode);
        }
    }

    private void notifyWorkMode(WorkMode mode) {
        if (workModeCallback == null) {
            return;
        }
        if (macConnected != null && DeviceManager.getInstance().getCurrentMac() != null) {
            boolean equals = TextUtils.equals(macConnected, DeviceManager.getInstance().getCurrentMac());
            if (!equals) {
                return;
            }
            workModeCallback.onResult(mode);
        }
    }

    private void notifyTimingMode(byte[] mode) {
        if (timingCallback == null) {
            return;
        }
        timingCallback.onResult(mode);
    }

    private void notifyIs3DMode(boolean is3D) {
        if (is3DCallback == null) {
            return;
        }
        if (macConnected != null && DeviceManager.getInstance().getCurrentMac() != null) {
            boolean equals = TextUtils.equals(macConnected, DeviceManager.getInstance().getCurrentMac());
            if (!equals) {
                return;
            }
            is3DCallback.onResult(false);
        }

    }

    private void notifyLightsMode(LightsMode mode) {
        if (lightsModeCallback == null) {
            return;
        }
        lightsModeCallback.onResult(mode);
    }

    private void notifyHearingMode(HearingMode mode) {
        if (hearingModeCallback == null) {
            return;
        }
        hearingModeCallback.onResult(mode);
    }

    private void notifyVolume() {
        if (volumeCallback == null) {
            return;
        }
        volumeCallback.onResult(volumeChangeManager.getVolumePercent());
    }

    private void notifyPlayState(boolean isPlay) {
        if (playStateCallback == null) {
            return;
        }
        playStateCallback.onResult(isPlay);
    }

    private void dealCustomData(byte[] data) {
        if (data.length < 2) {
            return;
        }
        if (data[0] == 2) {
            Log.d(TAG, "dealCustomData: " + data[1]);
            if (data[1] == 255) {
                notifyIs3DMode(data[2] == 1);
            } else if (data[1] == 5) {
                notifyWorkMode(data[2] == 1 ? WorkMode.SPATIAL : WorkMode.GAME);
            }
        }
    }

    private void sendCustomCmd(byte[] cmdData) {
        CommandBase customCmd = CommandBuilder.buildCustomCmd(cmdData);
        controller.sendRcspCommand(controller.getUsingDevice(), customCmd, new RcspCommandCallback() {
            @Override
            public void onCommandResponse(BluetoothDevice device, CommandBase cmd) {
                if (cmd.getStatus() != StateCode.STATUS_SUCCESS) { //固件回复失败状态
                    BaseError error = new BaseError(ErrorCode.SUB_ERR_RESPONSE_BAD_STATUS, "Device response an bad status : " + cmd.getStatus());
                    error.setOpCode(Command.CMD_EXTRA_CUSTOM);
                    onErrCode(device, error);
                    return;
                }
                //发送成功回调, 需要回复设备
                CustomCmd customCmd = (CustomCmd) cmd;
                byte[] responseData = new byte[0]; //可以设置回复的数据
                customCmd.setParam(new CustomParam(responseData));
                customCmd.setStatus(StateCode.STATUS_SUCCESS);
                controller.sendRcspResponse(device, customCmd); //发送命令回复
            }

            @Override
            public void onErrCode(BluetoothDevice device, BaseError error) {
                //失败回调
                //error - 错误信息
                Log.i(TAG, "onErrCode: " + error.toString());
            }
        });
    }
}
