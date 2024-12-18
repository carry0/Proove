package com.proove.smart.model;


import com.proove.ble.data.DeviceInfo;

import java.util.List;

public interface DeviceScanModel {
    interface IDeviceListChangeCallback {
        void onDeviceListChange(List<DeviceInfo> deviceInfoList);
    }

    interface IResultCallBack {
        void onSuccess();

        void onFail();
    }

    void startBleScan(IDeviceListChangeCallback callback);

    void stopBleScan();

    void connectDevice(String mac, IResultCallBack callBack);
    void connectBle(String mac,IResultCallBack callBack);
    void onClean();
    void release();
}
