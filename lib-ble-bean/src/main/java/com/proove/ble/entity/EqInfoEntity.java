package com.proove.ble.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.List;

@Entity(tableName = "eq_info", primaryKeys = {"device_tag", "custom_index"})
public class EqInfoEntity {
    @ColumnInfo(name = "device_tag")
    @NonNull
    private String deviceTag; //所属设备TAG 根据需求与设备关联

    @ColumnInfo(name = "name")
    @NonNull
    private String name; //eq名称

    @ColumnInfo(name = "is_custom")
    private boolean isCustom; //是否自定义EQ

    @ColumnInfo(name = "custom_index")
    private int customIndex; //自定义EQ的编号

    @ColumnInfo(name = "freq")
    private List<Integer> freq; //eq freq数据

    @ColumnInfo(name = "gain")
    private List<Float> gain; //eq gain数据

    public String getDeviceTag() {
        return deviceTag;
    }

    public void setDeviceTag(String deviceTag) {
        this.deviceTag = deviceTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public int getCustomIndex() {
        return customIndex;
    }

    public void setCustomIndex(int customIndex) {
        this.customIndex = customIndex;
    }

    public List<Integer> getFreq() {
        return freq;
    }

    public void setFreq(List<Integer> freq) {
        this.freq = freq;
    }

    public List<Float> getGain() {
        return gain;
    }

    public void setGain(List<Float> gain) {
        this.gain = gain;
    }

    public float[] getGainArray() {
        float[] gainArray = new float[gain.size()];
        for (int i = 0;i<gainArray.length;i++) {
            gainArray[i] = gain.get(i);
        }
        return gainArray;
    }
}
