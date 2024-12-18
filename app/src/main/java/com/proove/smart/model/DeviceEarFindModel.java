package com.proove.smart.model;


import com.proove.ble.constant.DeviceFindState;
import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.data.DeviceFindInfo;

public interface DeviceEarFindModel {
    interface IDataCallback<T> {
        void onResult(T data);
    }

    interface IResultCallback {
        void onSuccess();
        void onFail();
    }

    void onClean();
    void getDeviceFindInfo();
    void setDeviceFindInfoListener(IDataCallback<DeviceFindInfo> callback);
    void setDeviceFindRing(DeviceSide side, DeviceFindState state, IResultCallback resultCallback);
    boolean isPlaying();
}
