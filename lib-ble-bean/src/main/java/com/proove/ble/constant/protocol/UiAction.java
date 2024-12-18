package com.proove.ble.constant.protocol;

public enum UiAction {
    NONE(0x00),
    CLICK(0x01),
    DOUBLE_CLICK(0x02),
    TRIPLE_HIT(0x03),
    LONG_PRESS(0x04);

    private final int value;

    private static final UiAction[] VALUES = values();

    UiAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UiAction valueOf(int value) {
        for (UiAction state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }
        return NONE;
    }
}
