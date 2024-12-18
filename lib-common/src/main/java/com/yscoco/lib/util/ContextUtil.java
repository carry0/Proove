package com.yscoco.lib.util;

import android.content.Context;

public class ContextUtil {
    private static Context appContext;

    public static void setAppContext(Context context) {
        appContext = context;
    }

    public static Context getAppContext() {
        return appContext;
    }

}
