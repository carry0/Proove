package com.yscoco.lib.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.yscoco.lib.constant.SpConstant;

public class SpUtil {
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;
    private static SpUtil instance;

    private SpUtil() {
        preferences = ContextUtil.getAppContext().getSharedPreferences(SpConstant.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static SpUtil getInstance() {
        if (instance == null) {
            instance = new SpUtil();
        }
        return instance;
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }
}
