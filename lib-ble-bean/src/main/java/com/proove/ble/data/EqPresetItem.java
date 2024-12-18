package com.proove.ble.data;


import com.proove.ble.constant.EqPreset;

public class EqPresetItem {
    EqPreset eqPreset;
    boolean isChecked;

    public EqPresetItem() {
    }

    public EqPresetItem(EqPreset eqPreset, boolean isChecked) {
        this.eqPreset = eqPreset;
        this.isChecked = isChecked;
    }

    public EqPreset getEqPreset() {
        return eqPreset;
    }

    public void setEqPreset(EqPreset eqPreset) {
        this.eqPreset = eqPreset;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public String toString() {
        return "EqPresetItem{" +
                "eqPreset=" + eqPreset +
                ", isChecked=" + isChecked +
                '}';
    }
}
