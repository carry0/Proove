package com.proove.ble.constant;

public enum DeviceFindState {
    RINGING(0x01),
    NORMAL(0x00);

    private final int value;

    private static final DeviceFindState[] VALUES = values();

    DeviceFindState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DeviceFindState valueOf(int value) {
        for (DeviceFindState result : VALUES) {
            if (result.value == value) {
                return result;
            }
        }
        return null;
    }
}
