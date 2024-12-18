package com.proove.ble.data;


import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.constant.protocol.UiAction;
import com.proove.ble.constant.protocol.UiFunction;

public class KeySettingsData {
    private final DeviceSide deviceSide;
    private final UiAction uiAction;
    private final UiFunction uiFunction;

    public KeySettingsData(DeviceSide deviceSide, UiAction uiAction, UiFunction uiFunction) {
        this.deviceSide = deviceSide;
        this.uiAction = uiAction;
        this.uiFunction = uiFunction;
    }

    public DeviceSide getDeviceSide() {
        return deviceSide;
    }

    public UiAction getUiAction() {
        return uiAction;
    }

    public UiFunction getUiFunction() {
        return uiFunction;
    }
}
