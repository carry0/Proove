package com.proove.ble.constant.protocol;

public enum UiFunction {
    NONE(0x00),
    VOICE_ASSISTANT(0x0C),
    PREVIOUS_SONG(0x03),
    NEXT_SONG(0x04),
    PLAY_OR_PAUSE(0x05),
    INCREASE_VOLUME(0x09),
    DECREASE_VOLUME(0x0A),
    WORK_MODE(0x0D),
    ANC(0xFF);

    private final int value;

    private static final UiFunction[] VALUES = values();

    UiFunction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UiFunction valueOf(int value) {
        for (UiFunction state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }

        return NONE;
    }
}
