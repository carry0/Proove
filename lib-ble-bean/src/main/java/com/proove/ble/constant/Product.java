package com.proove.ble.constant;



import com.proove.ble.R;
import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.constant.protocol.UiAction;
import com.proove.ble.constant.protocol.UiFunction;
import com.proove.ble.data.KeySettingsData;

import java.util.List;

public enum Product {
    BTW98 {
        @Override
        public int getProductId() {
            return 0x0001;
        }

        @Override
        public int getProductImage() {
            return R.drawable.image_product_ear;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.image_product_ear;
        }

        @Override
        public int getProductIconEarL() {
            return 0;
        }

        @Override
        public int getProductIconEarR() {
            return 0;
        }

        @Override
        public String getProductName() {
            return "Proove 808 Power";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }

    },
    J50 {
        @Override
        public int getProductId() {
            return 0x0002;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_j50_all;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_j50_case;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_j50_l;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_j50_r;
        }

        @Override
        public String getProductName() {
            return "Proove Digital";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        public boolean isSupport3DMode() {
            return false;
        }
        @Override
        public boolean isSupport() {
            return true;
        }

        public List<UiFunction> getUiFunction() {
            return List.of(UiFunction.NONE, UiFunction.PLAY_OR_PAUSE,
                    UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                    UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.VOICE_ASSISTANT);
        }
    },
    A3Pro {
        @Override
        public int getProductId() {
            return 0x0003;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_a3pro_all;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_a3pro_case;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_a3pro_l;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_a3pro_r;
        }

        @Override
        public String getProductName() {
            return "Proove Digital Pro";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }

        public boolean isSupport3DMode() {
            return false;
        }

        @Override
        public AncType getAncType() {
            return AncType.ALL;
        }

    },
    A3Pro_WHITE {
        @Override
        public int getProductId() {
            return 0x000C;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_a3pro_all_white;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_a3pro_case_white;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_a3pro_l_white;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_a3pro_r_white;
        }

        @Override
        public String getProductName() {
            return "Proove Digital Pro";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }

        public boolean isSupport3DMode() {
            return false;
        }

        @Override
        public AncType getAncType() {
            return AncType.ALL;
        }

    },
    Y60 {
        @Override
        public int getProductId() {
            return 0x0004;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_y60_all;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_y60_case;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_y60_l;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_y60_r;
        }

        @Override
        public String getProductName() {
            return "Proove Intro";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }

        @Override
        public boolean isSupport3DMode() {
            return false;
        }


        public List<UiFunction> getUiFunction() {
            return List.of(UiFunction.NONE,  UiFunction.PLAY_OR_PAUSE,
                    UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                    UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.VOICE_ASSISTANT);
        }
    },
    Y60_WHITE {
        @Override
        public int getProductId() {
            return 0x000A;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_y60_all_white;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_y60_case_white;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_y60_l_white;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_y60_r_white;
        }

        @Override
        public String getProductName() {
            return "Proove Intro";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

        public List<UiFunction> getUiFunction() {
            return List.of(UiFunction.NONE, UiFunction.PLAY_OR_PAUSE,
                    UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                    UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.VOICE_ASSISTANT);
        }

    },
    Y90 {
        @Override
        public int getProductId() {
            return 0x0005;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_y90_all;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_y90_all;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_y90_l;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_y90_r;
        }

        @Override
        public String getProductName() {
            return "Proove Intro Pro";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }
        public AncType getAncType() {
            return AncType.ALL;
        }

    },
    XY30 {
        @Override
        public int getProductId() {
            return 0x0006;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_xy30_all;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_xy30_case;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_xy30_l;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_xy30_r;
        }

        @Override
        public String getProductName() {
            return "Proove Charm";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }

        @Override
        public boolean isSupport3DMode() {
            return false;
        }

        public List<UiFunction> getUiFunction() {
            return List.of(UiFunction.NONE, UiFunction.PLAY_OR_PAUSE,
                    UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                    UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.VOICE_ASSISTANT);
        }

    },
    L21 {
        @Override
        public int getProductId() {
            return 0x0007;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_l21_all;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_l21_case;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_l21_l;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_l21_r;
        }

        @Override
        public String getProductName() {
            return "Proove Logic";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

        public List<UiFunction> getUiFunction() {
            return List.of(UiFunction.NONE, UiFunction.PLAY_OR_PAUSE,
                    UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                    UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.VOICE_ASSISTANT);
        }

    },
    L21_WHITE {
        @Override
        public int getProductId() {
            return 0x000E;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_l21_all_white;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_l21_case_white;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_l21_l_white;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_l21_r_white;
        }

        @Override
        public String getProductName() {
            return "Proove Logic";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

        public List<UiFunction> getUiFunction() {
            return List.of(UiFunction.NONE, UiFunction.PLAY_OR_PAUSE,
                    UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                    UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.VOICE_ASSISTANT);
        }

    },
    AFD_50 {
        @Override
        public int getProductId() {
            return 0x0000;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_mainstream_pro_all_white;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_mainstream_pro_case_white;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_mainstream_pro_l_white;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_mainstream_pro_r_white;
        }

        @Override
        public String getProductName() {
            return "Proove Mainstream Pro";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

        public List<UiFunction> getUiFunction() {
            return List.of(UiFunction.NONE,UiFunction.PLAY_OR_PAUSE,
                    UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                    UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.VOICE_ASSISTANT);
        }

    },
    Blue_pods_104 {
        @Override
        public int getProductId() {
            return 0x0009;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_mainstream_all;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_mainstream_case;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_mainstream_l;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_mainstream_r;
        }

        @Override
        public String getProductName() {
            return "Proove Mainstream";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

        public List<UiFunction> getUiFunction() {
            return List.of(UiFunction.NONE, UiFunction.PLAY_OR_PAUSE,
                    UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                    UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.VOICE_ASSISTANT);
        }

    },
    Blue_pods_104_WHITE {
        @Override
        public int getProductId() {
            return 0x000D;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_mainstream_all;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_mainstream_case_white;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_mainstream_l_white;
        }

        @Override
        public int getProductIconEarR() {
            return R.drawable.ic_mainstream_r_white;
        }

        @Override
        public String getProductName() {
            return "Proove Mainstream";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

        public List<UiFunction> getUiFunction() {
            return List.of(UiFunction.NONE, UiFunction.PLAY_OR_PAUSE,
                    UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                    UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.VOICE_ASSISTANT);
        }

    },
    GAMING_RAPTURE {
        @Override
        public int getProductId() {
            return 0x000B;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_gaming;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_gaming;
        }


        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_gaming;
        }

        @Override
        public int getProductIconEarR() {
            return 0;
        }

        @Override
        public String getProductName() {
            return "Proove Gaming Rapture";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }

        @Override
        public boolean isSupport3DMode() {
            return false;
        }

    },
    GAMING_RAPTURE_WHITE {
        @Override
        public int getProductId() {
            return 0x000F;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_gaming_white;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_gaming_white;
        }

        @Override
        public int getProductIconEarL() {
            return R.drawable.ic_gaming_white;
        }

        @Override
        public int getProductIconEarR() {
            return 0;
        }

        @Override
        public String getProductName() {
            return "Proove Gaming Rapture";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }

        @Override
        public boolean isSupport3DMode() {
            return false;
        }

    },
    X8 {
        @Override
        public int getProductId() {
            return 0xAAAA;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_x8;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_x8;
        }

        @Override
        public int getProductIconEarL() {
            return 0;
        }

        @Override
        public int getProductIconEarR() {
            return 0;
        }

        @Override
        public String getProductName() {
            return "Proove X-City Pro";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

    },
    X9_Pro_Max {
        @Override
        public int getProductId() {
            return 0xBBBB;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_x9;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_x9;
        }

        @Override
        public int getProductIconEarL() {
            return 0;
        }

        @Override
        public int getProductIconEarR() {
            return 0;
        }

        @Override
        public String getProductName() {
            return "Proove X-City Pro Max";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

    },
    X10 {
        @Override
        public int getProductId() {
            return 0xCCCC;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_x10;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_x10;
        }

        @Override
        public int getProductIconEarL() {
            return 0;
        }

        @Override
        public int getProductIconEarR() {
            return 0;
        }

        @Override
        public String getProductName() {
            return "Proove Dual Sport";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

    },
    X11 {
        @Override
        public int getProductId() {
            return 0xDDDD;
        }

        @Override
        public int getProductImage() {
            return R.drawable.ic_x11;
        }

        @Override
        public int getProductIcon() {
            return R.drawable.ic_x11;
        }

        @Override
        public int getProductIconEarL() {
            return 0;
        }

        @Override
        public int getProductIconEarR() {
            return 0;
        }

        @Override
        public String getProductName() {
            return "Proove Urban";
        }

        @Override
        public DeviceType getType() {
            return DeviceType.DEVICE_TYPE_IN_EAR;
        }

        @Override
        public boolean isSupport() {
            return true;
        }


        @Override
        public boolean isSupport3DMode() {
            return false;
        }

    };



    public static final int PID_ALL = 0;

    public abstract int getProductId();

    public abstract int getProductImage();

    public abstract int getProductIcon();

    public abstract int getProductIconEarL();
    public abstract int getProductIconEarR();

    public abstract String getProductName();

    public abstract DeviceType getType();

    public abstract boolean isSupport();

    public boolean isSupportOta() {
        return false;
    }
    public boolean isSupportKeySet() {
        return true;
    }
    public boolean isSupportWorkMode() {
        return true;
    }
    public boolean isSupport3DMode() {
        return true;
    }
    public BatteryType getBatteryType() {
        return BatteryType.DOUBLE;
    }

    public AncType getAncType() {
        return AncType.NOT_SUPPORT;
    }

    public List<UiAction> getUiActions() {
        return List.of(UiAction.values());
    }

    public List<UiFunction> getUiFunction() {
        return List.of(UiFunction.NONE, UiFunction.PLAY_OR_PAUSE,
                UiFunction.PREVIOUS_SONG, UiFunction.NEXT_SONG, UiFunction.INCREASE_VOLUME,
                UiFunction.DECREASE_VOLUME,UiFunction.WORK_MODE,UiFunction.ANC,UiFunction.VOICE_ASSISTANT);
    }

    public List<KeySettingsData> getResetUi() {
        return List.of(
                new KeySettingsData(DeviceSide.LEFT, UiAction.CLICK, UiFunction.PLAY_OR_PAUSE),
                new KeySettingsData(DeviceSide.RIGHT, UiAction.CLICK, UiFunction.PLAY_OR_PAUSE),
                new KeySettingsData(DeviceSide.LEFT, UiAction.DOUBLE_CLICK, UiFunction.PREVIOUS_SONG),
                new KeySettingsData(DeviceSide.RIGHT, UiAction.DOUBLE_CLICK, UiFunction.NEXT_SONG),
                new KeySettingsData(DeviceSide.LEFT, UiAction.TRIPLE_HIT, UiFunction.DECREASE_VOLUME),
                new KeySettingsData(DeviceSide.RIGHT, UiAction.TRIPLE_HIT, UiFunction.INCREASE_VOLUME),
                new KeySettingsData(DeviceSide.LEFT, UiAction.LONG_PRESS, UiFunction.VOICE_ASSISTANT),
                new KeySettingsData(DeviceSide.RIGHT, UiAction.LONG_PRESS, UiFunction.WORK_MODE)
        );
    }

    public static Product getProductById(int id) {
        for (Product product : Product.values()) {
            if (product.getProductId() == id) {
                return product;
            }
        }
        return null;
    }

    public static Product getProductByName(String name) {
        for (Product product : Product.values()) {
            if (product.getProductName().equals(name)) {
                return product;
            }
        }
        return null;
    }

    public static boolean isSupportDevice(int pid) {
        for (Product product : Product.values()) {
            if (product.getProductId() == pid) {
                return true;
            }
        }
        return false;
    }
}
