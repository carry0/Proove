package com.yscoco.lib.util;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class LocationUtil {

    /**
     * 定位是否开启
     * @param ctx 上下文
     * @return true：开启
     */
    public static boolean isLocationOpen(Context ctx){
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void jumpLocationSetting(Context ctx) {
        Intent intent = new Intent(ACTION_LOCATION_SOURCE_SETTINGS);
        ctx.startActivity(intent);
    }
}
