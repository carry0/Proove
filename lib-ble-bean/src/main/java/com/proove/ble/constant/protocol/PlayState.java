package com.proove.ble.constant.protocol;

public enum PlayState {
    NORMAL(0x01),
    GAME(0x02);

    private final int value;

    PlayState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PlayState valueOf(int value) {
        for (PlayState mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        return NORMAL;
    }
}
