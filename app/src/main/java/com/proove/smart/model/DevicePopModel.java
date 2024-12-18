package com.proove.smart.model;


import com.proove.ble.data.DevicePopInfo;

public interface DevicePopModel {
    interface IDataCallback<T> {
        void onResult(T data);
    }

    interface IResultCallBack {
        void onSuccess();

        void onFail();
    }

    void onClean();

    void setDevicePopInfoListener(IDataCallback<DevicePopInfo> listener);

    void startScan();

    void stopScan();

    /**
     * 屏蔽设备一段时间
     *
     * @param mac 设备mac
     * @param time 屏蔽的时间，单位毫秒
     */
    void addDeviceToBlock(String mac, long time);

    void release();
    void connectDevice(String mac, IResultCallBack callBack);
}
