package com.proove.smart.vm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.proove.ble.constant.DeviceInfoType;
import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.constant.protocol.UiAction;
import com.proove.ble.constant.protocol.UiFunction;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.UiInfo;
import com.proove.smart.model.DeviceEarKeySettingModel;
import com.proove.smart.model.DeviceEarKeySettingModelImpl;
import com.proove.smart.manager.DeviceManager;

import java.util.List;

public class DeviceKeySettingViewModel extends ViewModel {
    private final MutableLiveData<Integer> _currentDialog = new MutableLiveData<>(-1);
    private final DeviceEarKeySettingModel model = new DeviceEarKeySettingModelImpl();
    private final MutableLiveData<Boolean> connectStateLiveData = new MutableLiveData<>();
    private DeviceSide deviceSide;
    private final MutableLiveData<UiInfo> uiInfoLiveData = new MutableLiveData<>();
    DeviceManager.IDeviceEventListener deviceEventListener = new DeviceManager.IDeviceEventListener() {
        @Override
        public void onDeviceInfoChange(DeviceInfoType type, DeviceInfo deviceInfo) {

        }

        @Override
        public void onDeviceListChange(List<DeviceInfo> deviceInfoList) {

        }

        @Override
        public void onConnectionChange(DeviceInfo device, int state) {
            if (device.getMac().equals(DeviceManager.getInstance().getCurrentMac())) {
                if (state == BluetoothAdapter.STATE_CONNECTED) {
                    connectStateLiveData.setValue(true);
                } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {
                    connectStateLiveData.setValue(false);
                }
            }
        }
    };

    public DeviceKeySettingViewModel() {
        DeviceManager.getInstance().addDeviceEventListener(deviceEventListener);
    }

    public MutableLiveData<Boolean> getConnectStateLiveData() {
        return connectStateLiveData;
    }

    public MutableLiveData<UiInfo> getUiInfoLiveData() {
        model.setUiInfoListener(uiInfoLiveData::setValue);
        return uiInfoLiveData;
    }

    public void getUIInfo() {
        model.getUiInfo();
    }


    public LiveData<Integer> getCurrentDialog() {
        return _currentDialog;
    }



    public void finishActivity(View view) {
        ((Activity) view.getContext()).finish();
    }


    public void setUi(DeviceSide side,UiAction uiAction, UiFunction function) {
        model.setUi(side, uiAction, function);
    }

    public void showDialog(int type) {
        _currentDialog.setValue(type);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        DeviceManager.getInstance().removeDeviceEventListener(deviceEventListener);
    }
    public DeviceSide getDeviceSide() {
        return deviceSide;
    }
    public void setDeviceSide(DeviceSide side) {
        this.deviceSide = side;
    }
}

