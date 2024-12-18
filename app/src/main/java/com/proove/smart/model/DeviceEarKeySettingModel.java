package com.proove.smart.model;


import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.constant.protocol.UiAction;
import com.proove.ble.constant.protocol.UiFunction;
import com.proove.ble.data.UiInfo;

public interface DeviceEarKeySettingModel {
    interface IDataListener<T> {
        void onResult(T data);
    }

    void onClean();

    void getUiInfo();

    void setUiInfoListener(IDataListener<UiInfo> listener);

    void setUi(DeviceSide side, UiAction action, UiFunction function);

    void resetUi(DeviceSide side);
}
