package com.proove.smart.vm;

import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.proove.ble.constant.Product;
import com.proove.ble.constant.protocol.AncMode;
import com.proove.ble.constant.protocol.HearingMode;
import com.proove.ble.constant.protocol.LightsMode;
import com.proove.ble.constant.protocol.WorkMode;
import com.proove.ble.data.DeviceInfo;
import com.proove.smart.model.DeviceEarModel;
import com.proove.smart.model.DeviceEarModelImpl;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.ui.DeviceKeySettingActivity;
import com.proove.smart.ui.DeviceOtaUpdateActivity;
import com.proove.ble.data.BatteryInfo;

public class DeviceEarInfoViewModel extends ViewModel {
    private MutableLiveData<Boolean> connectStateLiveData;
    private MutableLiveData<Boolean> is3DStateLiveData;
    private MutableLiveData<BatteryInfo> batteryLiveData = new MutableLiveData<>();
    private MutableLiveData<AncMode> ancModeLiveData = new MutableLiveData<>();
    private MutableLiveData<WorkMode> workModeLiveData = new MutableLiveData<>();
    private final DeviceEarModel model = new DeviceEarModelImpl();

    public MutableLiveData<Boolean> getIs3DStateLiveData() {
        if (is3DStateLiveData == null) {
            is3DStateLiveData = new MutableLiveData<>();
            model.setIs3DCallback(is3d -> is3DStateLiveData.postValue(is3d));
        }
        return is3DStateLiveData;
    }

    public MutableLiveData<Boolean> getConnectStateLiveData() {
        if (connectStateLiveData == null) {
            connectStateLiveData = new MutableLiveData<>();
            model.setConnectStatusListener(isConnected -> connectStateLiveData.postValue(isConnected));
        }
        return connectStateLiveData;
    }

    public MutableLiveData<BatteryInfo> getBatteryLiveData() {
        model.setBatteryListener(battery -> batteryLiveData.setValue(battery));
        return batteryLiveData;
    }

    public void getBattery() {
        model.getBattery();
    }
    public void get3DMode(){
        model.get3DMode();
    }

    public MutableLiveData<AncMode> getAncModeLiveData() {
        model.setAncModeListener(ancMode -> ancModeLiveData.setValue(ancMode));
        return ancModeLiveData;
    }

    public void getAncMode() {
        model.getAncMode();
    }

    public MutableLiveData<WorkMode> getWorkModeLiveData() {
        model.setWorkModeListener(workMode -> workModeLiveData.setValue(workMode));
        return workModeLiveData;
    }

    public void getWorkMode() {
        model.getWorkMode();
    }

    public void setConnectMac(String mac) {
        model.setConnectMac(mac);
    }

    private long lastClickTime = 0;
    private MutableLiveData<Boolean> unOnClick = new MutableLiveData<>();

    public MutableLiveData<Boolean> getUnOnClick() {
        return unOnClick;
    }

    public void setAncMode(AncMode mode) {
        if (!isTime()) {
            return;
        }
        model.setAncMode(mode);
    }
    public void set3DMode(boolean is3D){
        if (!isTime()) {
            return;
        }
        model.set3DMode(is3D);
    }

    public void setWorkMode(WorkMode workMode) {
        if (!isTime()) {
            return;
        }
        model.setWorkMode(workMode);
    }

    public void editDeviceName(String name, DeviceEarModel.IDataCallback<Boolean> callback) {
        model.setDeviceName(name, callback);
    }

    public void rebootDevice() {
        model.rebootDevice();
    }

    public void getDeviceResetMode() {
        DeviceInfo deviceInfo = DeviceManager.getInstance().getCurrentDeviceInfo();


        if (deviceInfo != null) {
            Product product =  DeviceManager.getInstance().getCurrentProduct();
            if (product!=null){
                DeviceManager.getInstance().saveDeviceName(deviceInfo.getMac(), product.getProductName());
            }
        }
        model.resetEqData();
        model.getDeviceResetMode();
    }

    private boolean isTime() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime <= 1000) {
            unOnClick.setValue(false);
            return false;
        }
        lastClickTime = currentTime;
        unOnClick.setValue(true);
        return true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        model.onClean();
    }


    public void deleteDevice(String stringExtra) {
        model.deleteDevice(stringExtra);
    }
}

