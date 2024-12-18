package com.proove.smart.model;



import static com.proove.ble.constant.EqConstant.CUSTOM_INDEX_1;
import static com.proove.ble.constant.EqConstant.CUSTOM_INDEX_2;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.room.Room;

import com.jieli.bluetooth.bean.base.BaseError;
import com.jieli.bluetooth.bean.device.eq.EqInfo;
import com.jieli.bluetooth.bean.device.eq.EqPresetInfo;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.rcsp.callback.BTRcspEventCallback;
import com.jieli.bluetooth.interfaces.rcsp.callback.OnRcspActionCallback;
import com.proove.ble.constant.CommonEqInfo;
import com.proove.ble.constant.Constant;
import com.proove.ble.constant.EqConstant;
import com.proove.ble.constant.EqPreset;
import com.proove.ble.constant.SpConstant;
import com.proove.ble.entity.EqInfoEntity;
import com.proove.smart.database.AppDatabase;
import com.proove.smart.manager.DeviceManager;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.ContextUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.SpUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EqModelImpl implements EqModel {
    public static final String TAG = "EqModelJLImpl";

    private final RCSPController controller = RCSPController.getInstance();
    private IDataListener<CommonEqInfo> eqInfoListener;
    private final CommonEqInfo mCommonEqInfo = new CommonEqInfo();
    private EqPresetInfo mEqPresetInfo;
    private AppDatabase database;
    private final Handler handler;
    private final HandlerThread handlerThread = new HandlerThread(TAG);
    private final BTRcspEventCallback btRcspEventCallback = new BTRcspEventCallback() {
        @Override
        public void onEqChange(BluetoothDevice device, EqInfo eqInfo) {
            LogUtil.info(TAG, "onEqChange = " + eqInfo);
            EqPreset eqPreset;
            if (eqInfo.getMode() < EqConstant.PRESET_MAX) {
                eqPreset = EqPreset.valueOf(eqInfo.getMode());
                CommonEqInfo tempCommonEqInfo = new CommonEqInfo();
                tempCommonEqInfo.setIndex(eqPreset.getIndex());
                tempCommonEqInfo.setFreq(eqInfo.getFreqs());
                float[] gain = new float[eqInfo.getCount()];
                for (int i = 0; i < gain.length; i++) {
                    gain[i] = eqInfo.getValue()[i];
                }
                tempCommonEqInfo.setGain(gain);
                saveLastCheckedIndex(eqInfo.getMode());
                notifyEqInfo(tempCommonEqInfo);
            } else {
                int checkedEqIndex = getCheckedEqIndex();
                if (checkedEqIndex < EqConstant.PRESET_MAX) {

                    CommonEqInfo tempCommonEqInfo = new CommonEqInfo();
                    tempCommonEqInfo.setIndex(CUSTOM_INDEX_1);
                    tempCommonEqInfo.setEqName("Custom Sound 1");
                    tempCommonEqInfo.setFreq(eqInfo.getFreqs());
                    float[] gain = new float[eqInfo.getCount()];
                    for (int i = 0; i < gain.length; i++) {
                        gain[i] = eqInfo.getValue()[i];
                    }
                    tempCommonEqInfo.setGain(gain);
                    saveLastCheckedIndex(CUSTOM_INDEX_1);
                    saveEqData(CUSTOM_INDEX_1, eqInfo.getFreqs(), gain);
                    notifyEqInfo(tempCommonEqInfo);
                    return;
                }
                getEqData(checkedEqIndex, new IEqInfoEntityCallback() {
                    @Override
                    public void onResult(EqInfoEntity data) {
                        CommonEqInfo tempEqInfo = new CommonEqInfo();
                        tempEqInfo.setIndex(checkedEqIndex);
                        if(checkedEqIndex==CUSTOM_INDEX_1){
                            tempEqInfo.setEqName("Custom Sound 1");
                        }else if (checkedEqIndex==CUSTOM_INDEX_2){
                            tempEqInfo.setEqName("Custom Sound 2");
                        }
                        tempEqInfo.setFreq(EqConstant.DEFAULT_FREQ);
                        float[] gain = new float[EqConstant.DEFAULT_FREQ.length];
                        for (int i = 0; i < gain.length; i++) {
                            gain[i] = data.getGain().get(i);
                        }
                        tempEqInfo.setGain(gain);
                        notifyEqInfo(tempEqInfo);
                    }

                    @Override
                    public void onFail() {
                        CommonEqInfo tempEqInfo = new CommonEqInfo();
                        tempEqInfo.setIndex(checkedEqIndex);
                        tempEqInfo.setFreq(EqConstant.DEFAULT_FREQ);
                        tempEqInfo.setGain(EqConstant.DEFAULT_GAIN);
                        notifyEqInfo(tempEqInfo);
                    }
                });
            }
        }

        @Override
        public void onEqPresetChange(BluetoothDevice device, EqPresetInfo eqPresetInfo) {
            LogUtil.info(TAG, "onEqPresetChange = " + eqPresetInfo);
            mEqPresetInfo = eqPresetInfo;
        }
    };

    public EqModelImpl() {
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {
            database = Room
                    .databaseBuilder(ContextUtil.getAppContext(), AppDatabase.class, Constant.APP_DATABASE_NAME)
                    .build();
        });
        controller.addBTRcspEventCallback(btRcspEventCallback);
    }

    @Override
    public void onClean() {
        database.close();
        handlerThread.quitSafely();
        controller.removeBTRcspEventCallback(btRcspEventCallback);
    }

    @Override
    public void getEqInfo() {
        controller.getEqInfo(controller.getUsingDevice(), new OnRcspActionCallback<Boolean>() {
            @Override
            public void onSuccess(BluetoothDevice device, Boolean message) {
                LogUtil.info(TAG, "getEqDataFromDevice onSuccess");
            }

            @Override
            public void onError(BluetoothDevice device, BaseError error) {
                LogUtil.info(TAG, "getEqDataFromDevice onError " + error.getMessage());
            }
        });
    }

    @Override
    public void setEqInfoListener(IDataListener<CommonEqInfo> listener) {
        eqInfoListener = listener;
    }

    @Override
    public void setEq(int index, int[] freq, float[] gain) {
        EqInfo eqInfo = new EqInfo();
        eqInfo.setFreqs(freq);
        byte[] gainBytes = new byte[gain.length];
        for (int i = 0; i < gainBytes.length; i++) {
            gainBytes[i] = (byte) Math.round(gain[i]);
        }
        eqInfo.setMode(EqConstant.CUSTOM_INDEX_JL); //6 自定义模式
        eqInfo.setValue(gainBytes);
        LogUtil.info(TAG, "setEqDataToDevice = " + eqInfo);
        controller.configEqInfo(controller.getUsingDevice(), eqInfo, new OnRcspActionCallback<>() {
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

    @Override
    public void setEqPreset(EqPreset eqPreset) {
        saveLastCheckedIndex(eqPreset.getIndex());
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
        LogUtil.info(TAG, "setEqDataToDevice = " + targetEqInfo);
        controller.configEqInfo(controller.getUsingDevice(), targetEqInfo, new OnRcspActionCallback<>() {
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

    @Override
    public void setEqCustom(int index) {
        saveLastCheckedIndex(index);
        getEqData(index, new EqModel.IEqInfoEntityCallback() {
            @Override
            public void onResult(EqInfoEntity data) {
                float[] gains = new float[EqConstant.DEFAULT_FREQ.length];
                for (int i = 0; i < gains.length; i++) {
                    gains[i] = data.getGain().get(i);
                }
                setEq(EqConstant.CUSTOM_INDEX_JL, EqConstant.DEFAULT_FREQ, gains);
            }

            @Override
            public void onFail() {
                setEq(EqConstant.CUSTOM_INDEX_JL, EqConstant.DEFAULT_FREQ, EqConstant.DEFAULT_GAIN);
            }
        });
    }

    @Override
    public void saveEqData(int index, int[] freq, float[] gain) {
        String mac = DeviceManager.getInstance().getCurrentMac();
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return;
        }
        handler.post(() -> {
            EqInfoEntity eqInfoEntity = new EqInfoEntity();
            eqInfoEntity.setDeviceTag(mac);
            eqInfoEntity.setCustomIndex(index);
            eqInfoEntity.setName(String.valueOf(index));
            eqInfoEntity.setFreq(Arrays.stream(freq).boxed().collect(Collectors.toList()));
            List<Float> gainList = new ArrayList<>();
            for (float gainF : gain) {
                gainList.add(gainF);
            }
            eqInfoEntity.setGain(gainList);
            database.eqInfoDao().addEqInfo(eqInfoEntity);
        });
    }
    @Override
    public void editEQName(int index, String EQName) {
        String mac = DeviceManager.getInstance().getCurrentMac();
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return;
        }
        handler.post(() -> {
            EqInfoEntity eqInfo = database.eqInfoDao().getEqInfo(mac, index);
            eqInfo.setName(EQName);
            database.eqInfoDao().addEqInfo(eqInfo);
        });
    }
    @Override
    public void getEQName(IEqNameCallback callback) {
        handler.post(() -> {
            List<EqInfoEntity> eqInfoList = database.eqInfoDao().getEqInfoList();
            if (eqInfoList==null){
                callback.onFail();
            }else {
                List<String> eqNameList = new ArrayList<>();
                for (EqInfoEntity entity:eqInfoList){
                    if (entity.getName()==null&&entity.getName().isEmpty()){
                        continue;
                    }
                    eqNameList.add(entity.getName());
                }
                callback.onResult(eqNameList);
            }

        });
    }

    private void notifyEqInfo(CommonEqInfo commonEqInfo) {
        if (eqInfoListener == null) {
            return;
        }
        eqInfoListener.onResult(commonEqInfo);
    }

    private void getEqData(int index, IEqInfoEntityCallback callBack) {
        String mac = DeviceManager.getInstance().getCurrentMac();
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return;
        }
        handler.post(() -> {
            EqInfoEntity eqInfoEntity = database.eqInfoDao().getEqInfo(mac, index);
            if (eqInfoEntity != null) {
                callBack.onResult(eqInfoEntity);
            } else {
                callBack.onFail();
            }
        });
    }

    private int getCheckedEqIndex() {
        String mac = DeviceManager.getInstance().getCurrentMac();
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return EqPreset.NATURE.getIndex();
        }
        return SpUtil.getInstance()
                .getInt(SpConstant.getEqCheckedSpKey(mac), EqPreset.NATURE.getIndex());
    }

    private void saveLastCheckedIndex(int index) {
        String mac = DeviceManager.getInstance().getCurrentMac();
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return;
        }
        SpUtil.getInstance().putInt(SpConstant.getEqCheckedSpKey(mac), index);
    }
}
