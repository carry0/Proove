package com.proove.ble.data;


import com.proove.ble.constant.DeviceFindState;

public class DeviceFindInfo {
    private DeviceFindState leftState;
    private DeviceFindState rightState;

    public DeviceFindInfo(byte[] bytes) {
        leftState = DeviceFindState.valueOf(bytes[0]);
        rightState = DeviceFindState.valueOf(bytes[1]);
    }

    public DeviceFindState getLeftState() {
        return leftState;
    }

    public void setLeftState(DeviceFindState leftState) {
        this.leftState = leftState;
    }

    public DeviceFindState getRightState() {
        return rightState;
    }

    public void setRightState(DeviceFindState rightState) {
        this.rightState = rightState;
    }

    @Override
    public String toString() {
        return "DeviceFindInfo{" +
                "leftState=" + leftState +
                ", rightState=" + rightState +
                '}';
    }
}
