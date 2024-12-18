package com.proove.smart.database;

import androidx.room.TypeConverter;

import com.google.gson.reflect.TypeToken;
import com.yscoco.lib.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;

public class Converters {
    @TypeConverter
    public static List<Integer> strToListInt(String data) {
        return GsonUtil.fromJson(data, new TypeToken<ArrayList<Integer>>(){}.getType());
    }

    @TypeConverter
    public static List<Float> strToListFloat(String data) {
        return GsonUtil.fromJson(data, new TypeToken<ArrayList<Float>>(){}.getType());
    }

    @TypeConverter
    public static String listIntToStr(List<Integer> data) {
        return GsonUtil.toJson(data, new TypeToken<ArrayList<Integer>>(){}.getType());
    }

    @TypeConverter
    public static String listFloatToStr(List<Float> data) {
        return GsonUtil.toJson(data, new TypeToken<ArrayList<Float>>(){}.getType());
    }
}