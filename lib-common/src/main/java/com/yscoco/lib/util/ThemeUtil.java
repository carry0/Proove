package com.yscoco.lib.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtil {
    public static void setStatusBarTransparent(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
        systemUiVisibility =
                systemUiVisibility | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    public static void changeNightMode(@AppCompatDelegate.NightMode int nightMode) {
        if (nightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static boolean isDarkMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }


    @SuppressLint("InternalInsetResource")
    public static int getStatusBarHeight(Context context) {
        int height = 0;
        int resourceId = context.getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getApplicationContext().getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }
}
