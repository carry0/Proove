package com.proove.smart.vm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.proove.ble.data.DeviceListItem;
import com.proove.smart.model.DeviceListModel;
import com.proove.smart.model.DeviceListModelImpl;

import java.util.List;

public class DeviceMoreSettingViewModel extends ViewModel {
    private final DeviceListModel model = new DeviceListModelImpl();

    public void deleteDevice(String mac) {
        model.deleteDevice(mac);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        model.onClean();
    }
}
