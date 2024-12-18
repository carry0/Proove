package com.proove.smart.model;


import com.proove.ble.data.DeviceInfo;

import java.util.List;

public interface DeviceListModel {
    void getConnectStatus();

    interface IDeviceListCallBack {
        void onResult(List<DeviceInfo> deviceInfoList);
    }

    interface IDataCallback<T> {
        void onResult(T data);
    }

    interface IConnectCallBack {
        void onSuccess();

        void onFail();
    }

    void setConnectStatusListener(IDataCallback<Boolean> callback);

    void getDeviceInfoList(IDeviceListCallBack callBack);

    void connectDevice(String mac, IConnectCallBack callBack);

    void deleteDevice(String mac);

    void onClean();
}
