package com.proove.ble.constant.protocol;

public enum DeviceSide {
    LEFT(0x01),
    RIGHT(0x02),
    ALL(0x00);

    private final int value;

    private static final DeviceSide[] VALUES = values();

    DeviceSide(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DeviceSide valueOf(int value) {
        for (DeviceSide state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }
        return ALL;
    }
}
