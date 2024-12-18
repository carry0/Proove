package com.proove.ble.constant;

import java.util.Arrays;

public class CommonEqInfo {
    private EqPreset eqPreset;
    private String eqName;
    private int index;
    private int[] freq;
    private float[] gain;

    public EqPreset getEqPreset() {
        return eqPreset;
    }

    public void setEqPreset(EqPreset eqPreset) {
        this.eqPreset = eqPreset;
    }

    public int[] getFreq() {
        return freq;
    }

    public void setFreq(int[] freq) {
        this.freq = freq;
    }

    public float[] getGain() {
        return gain;
    }

    public void setGain(float[] gain) {
        this.gain = gain;
    }

    public String getEqName() {
        return eqName;
    }

    public void setEqName(String eqName) {
        this.eqName = eqName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "CommonEqInfo{" +
                "eqPreset=" + eqPreset +
                ", eqName='" + eqName + '\'' +
                ", index=" + index +
                ", freq=" + Arrays.toString(freq) +
                ", gain=" + Arrays.toString(gain) +
                '}';
    }
}
