package com.yscoco.lib.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class GsonUtil {
    private static final Gson gson = new Gson();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static String toJson(Object object, Type type) {
        return gson.toJson(object, type);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }
}
