package com.proove.ble.constant.protocol;

public enum AncMode {
    OFF(0x00),
    ANC(0x01),
    PASS(0x02),
    ERROR(0x0A);
    private final int value;

    AncMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AncMode valueOf(int value) {
        for (AncMode ancMode : values()) {
            if (ancMode.value == value) {
                return ancMode;
            }
        }
        return ERROR;
    }
}
