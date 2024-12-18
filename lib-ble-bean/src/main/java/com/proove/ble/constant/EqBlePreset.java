package com.proove.ble.constant;


import com.proove.ble.R;

public enum EqBlePreset {
    NATURE(0x00) {
        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public int getNameResId() {
            return R.string.eq_default;
        }
    },
    POP_MUSIC(0x01) {
        @Override
        public int getIndex() {
            return 1;
        }

        @Override
        public int getNameResId() {
            return R.string.pop_music;
        }
    },
    COUNTRY_MUSIC(0x02) {
        @Override
        public int getIndex() {
            return 2;
        }

        @Override
        public int getNameResId() {
            return R.string.country_music;
        }
    },
    JAZZ(0x03) {
        @Override
        public int getIndex() {
            return 3;
        }

        @Override
        public int getNameResId() {
            return R.string.jazz;
        }
    },
    SLOW_SONG(0x04) {
        @Override
        public int getIndex() {
            return 4;
        }

        @Override
        public int getNameResId() {
            return R.string.slow_song;
        }
    },
    CLASSIC(0x05) {
        @Override
        public int getIndex() {
            return 5;
        }

        @Override
        public int getNameResId() {
            return R.string.classical_music;
        }
    };

    public abstract int getIndex();

    public abstract int getNameResId();

    private final int value;

    EqBlePreset(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EqBlePreset valueOf(int value) {
        for (EqBlePreset preset : values()) {
            if (preset.getIndex() == value) {
                return preset;
            }
        }
        return NATURE;
    }
}
