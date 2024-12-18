package com.yscoco.lib.util;

import android.os.Build;

public class SystemUtil {
    public static final String TAG = "SystemUtil";
    public static final String BRAND_VIVO = "vivo";

    public static String getDeviceBrand() {
        String brand = Build.BRAND;
        LogUtil.info(TAG, "Device Brand = " + brand);
        return brand;
    }
}
