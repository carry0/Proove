package com.proove.ble.constant.protocol;

public enum HearingMode {
    OFF(0x00),
    ON(0x01);

    private final int value;

    HearingMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HearingMode valueOf(int value) {
        for (HearingMode ancMode : values()) {
            if (ancMode.value == value) {
                return ancMode;
            }
        }
        return OFF;
    }
}
