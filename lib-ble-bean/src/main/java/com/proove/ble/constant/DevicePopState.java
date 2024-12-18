package com.proove.ble.constant;

public enum DevicePopState {
    OFF(0x00),
    ON(0x01);

    private final int value;

    DevicePopState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DevicePopState valueOf(int value) {
        for (DevicePopState state : values()) {
            if (state.value == value) {
                return state;
            }
        }
        return ON;
    }
}
