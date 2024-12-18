package com.yscoco.lib.util;

import android.util.Log;

public class LogUtil {
    private static String APP_TAG = "AppLog";

    public static void setLogTag(String tag) {
        APP_TAG = tag;
    }

    public static void debug(String tag, String msg) {
        String newTAG = APP_TAG +" " + tag;
        Log.d(newTAG, msg);
    }
    public static void info(String tag, String msg) {
        String newTAG = APP_TAG +" " + tag;
        Log.i(newTAG, msg);
    }
    public static void error(String tag, String msg) {
        String newTAG = APP_TAG +" " + tag;
        Log.e(newTAG, msg);
    }
}
