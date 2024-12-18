package com.proove.smart.model;


import com.proove.ble.data.BluetoothDataParser;

public interface Model {

    interface IDataCallback<T> {
        void onResult(T data);
    }

    void setDeviceStatusIDataCallback(IDataCallback<BluetoothDataParser.DeviceStatus> callback);
    void setConnectStatusListener(IDataCallback<Boolean> callback);
}
