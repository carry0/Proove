package com.proove.ble.constant;


import com.proove.ble.R;

public enum EqPreset {
    NATURE {
        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public int getNameResId() {
            return R.string.eq_default;
        }
    },
    POP_MUSIC {
        @Override
        public int getIndex() {
            return 1;
        }

        @Override
        public int getNameResId() {
            return R.string.pop_music;
        }
    },
    COUNTRY_MUSIC {
        @Override
        public int getIndex() {
            return 2;
        }

        @Override
        public int getNameResId() {
            return R.string.country_music;
        }
    },
    JAZZ {
        @Override
        public int getIndex() {
            return 3;
        }

        @Override
        public int getNameResId() {
            return R.string.jazz;
        }
    },
    SLOW_SONG {
        @Override
        public int getIndex() {
            return 4;
        }

        @Override
        public int getNameResId() {
            return R.string.slow_song;
        }
    },
    CLASSIC {
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


    public static EqPreset valueOf(int value) {
        for (EqPreset preset : values()) {
            if (preset.getIndex() == value) {
                return preset;
            }
        }
        return NATURE;
    }
}
