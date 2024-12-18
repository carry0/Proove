package com.proove.ble.constant.protocol;

public enum TimingMode {
    STOP(0xFE),
    CHECK(0xFF);
    private final int value;
    private  byte[] timeData;

    public void setTimeData(byte[] timeData) {
        this.timeData = timeData;
    }

    public byte[] getTimeData() {
        return timeData;
    }

    TimingMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TimingMode valueOf(int value) {
        for (TimingMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        return STOP;
    }
}
