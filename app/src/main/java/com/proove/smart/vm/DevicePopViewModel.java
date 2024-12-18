package com.proove.smart.vm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.proove.ble.constant.DevicePopState;
import com.proove.ble.constant.SpConstant;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DevicePopInfo;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.model.DevicePopModel;
import com.proove.smart.model.DevicePopModelImpl;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.SpUtil;

import java.util.List;

public class DevicePopViewModel extends ViewModel {
    private MutableLiveData<DevicePopInfo> devicePopInfoLiveData;

    private final DevicePopModel model = new DevicePopModelImpl();

    public MutableLiveData<DevicePopInfo> getDevicePopInfoLiveData() {
        if (devicePopInfoLiveData == null) {
            devicePopInfoLiveData = new MutableLiveData<>();
            if (!isDevicePopupOn()) {
                return devicePopInfoLiveData;
            }
            model.setDevicePopInfoListener(data -> {
                devicePopInfoLiveData.postValue(data);
            });
        }
        return devicePopInfoLiveData;
    }

    public static boolean isDevicePopupOn() {
        int value = SpUtil.getInstance().getInt(SpConstant.AUTO_POPUP_STATE, DevicePopState.OFF.getValue());
        DevicePopState devicePopState = DevicePopState.valueOf(value);
        return devicePopState == DevicePopState.ON;
    }

    public static void setDevicePopState(DevicePopState devicePopState) {
        if (devicePopState == null) {
            return;
        }
        SpUtil.getInstance().putInt(SpConstant.AUTO_POPUP_STATE, devicePopState.getValue());
    }

    public void startScan() {
        if (isDevicePopupOn()) {
            model.startScan();
        }
    }

    public void stopScan() {
        model.stopScan();
    }

    public void addDeviceToBlock(String mac, long time) {
        model.addDeviceToBlock(mac, time);
    }

    public void connectDevice(String mac, DevicePopModel.IResultCallBack callBack) {
        model.connectDevice(mac, callBack);
    }

    public boolean isInnerConnect(String mac) {
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return false;
        }
        return RCSPController.getInstance().isDeviceConnected(BluetoothUtil.getBluetoothDevice(mac));
    }

    public boolean isAlreadyInDeviceList(String mac) {
        List<DeviceInfo> deviceInfoList = DeviceManager.getInstance().getDeviceInfoList();
        for (DeviceInfo deviceInfo : deviceInfoList) {
            return deviceInfo.getMac().equals(mac);
        }
        return false;
    }
}
