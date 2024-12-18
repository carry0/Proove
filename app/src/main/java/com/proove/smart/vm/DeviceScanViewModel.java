package com.proove.smart.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DeviceListItem;
import com.proove.smart.model.DeviceScanModel;
import com.proove.smart.model.DeviceScanModelImpl;

import java.util.ArrayList;
import java.util.List;

public class DeviceScanViewModel extends ViewModel {

    private  MutableLiveData<List<DeviceListItem>> scanListLiveData= new MutableLiveData<>();

    private final DeviceScanModel model = new DeviceScanModelImpl();

    public MutableLiveData<List<DeviceListItem>> getScanListLiveData() {
        return scanListLiveData;
    }

    public void scan() {
        model.startBleScan(deviceInfoList -> scanListLiveData.setValue(getItems(deviceInfoList)));
    }

    @NonNull
    private List<DeviceListItem> getItems(List<DeviceInfo> deviceInfoList) {
        List<DeviceListItem> deviceListItems = new ArrayList<>();
        for (DeviceInfo deviceInfo : deviceInfoList) {
            DeviceListItem deviceListItem = new DeviceListItem(deviceInfo);
            deviceListItem.setProductImageResId(deviceInfo.getImageResId());
            deviceListItems.add(deviceListItem);
        }
        return deviceListItems;
    }

    public void stop(){
        model.stopBleScan();
    }

    public void connectDevice(String mac, DeviceScanModel.IResultCallBack connectCallBack) {
        model.connectDevice(mac ,connectCallBack);
    }

}
