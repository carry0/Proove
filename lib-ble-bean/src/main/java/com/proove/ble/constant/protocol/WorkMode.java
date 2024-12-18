package com.proove.ble.constant.protocol;

public enum WorkMode {
    NORMAL(0x01),
    GAME(0x02),

    SPATIAL(0x03);
    private final int value;

    WorkMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static WorkMode valueOf(int value) {
        for (WorkMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        return NORMAL;
    }
}
