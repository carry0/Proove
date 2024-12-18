package com.proove.ble.constant.protocol;

/**
 * 灯效控制
 * 0x00 关闭
 * 0x01 常亮
 * 0x02 呼吸
 */
public enum LightsMode {
    ON(0x01),
    OFF(0x00),
    BREATHE(0x02);

    private final int value;

    LightsMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static LightsMode valueOf(int value) {
        for (LightsMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        return OFF;
    }
}
