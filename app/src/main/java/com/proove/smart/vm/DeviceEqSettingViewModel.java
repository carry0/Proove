package com.proove.smart.vm;

import androidx.lifecycle.MutableLiveData;

import com.proove.ble.constant.CommonEqInfo;
import com.proove.ble.constant.EqConstant;
import com.proove.ble.constant.EqPreset;
import com.proove.smart.model.EqModel;
import com.proove.smart.model.EqModelImpl;

import java.util.List;

public class DeviceEqSettingViewModel extends DeviceKeySettingViewModel {
    private MutableLiveData<CommonEqInfo> eqInfoLiveData;

    private MutableLiveData<List<String>> eqNameListLiveData;

    public MutableLiveData<List<String>> getEqNameListLiveData() {
        if (eqNameListLiveData == null) {
            eqNameListLiveData = new MutableLiveData<>();
            model.getEQName(new EqModel.IEqNameCallback() {
                @Override
                public void onResult(List<String> data) {
                    eqNameListLiveData.postValue(data);
                }

                @Override
                public void onFail() {

                }
            });
        }
        return eqNameListLiveData;
    }

    private final EqModel model = new EqModelImpl();

    public MutableLiveData<CommonEqInfo> getEqInfoLiveData() {
        if (eqInfoLiveData == null) {
            eqInfoLiveData = new MutableLiveData<>();
            model.setEqInfoListener(data -> eqInfoLiveData.postValue(data));
            getEqInfo();
        }
        return eqInfoLiveData;
    }

    private void getEqInfo() {
        model.getEqInfo();
    }
    public void setEq(int index, float[] gain) {
        model.setEq(index, EqConstant.DEFAULT_FREQ, gain);
    }

    public void saveEqData(int index, float[] gain) {
        model.saveEqData(index, EqConstant.DEFAULT_FREQ, gain);
    }
    public void editEQName(int index, String EQName) {
        model.editEQName(index, EQName);
    }

    public void getEQName() {

    }
    public void setEqCustom(int index) {
        model.setEqCustom(index);
    }

    public EqPreset eqPreset;

    public EqPreset getEqPreset() {
        return eqPreset;
    }

    public void setEqPreset(EqPreset eqPreset) {
        this.eqPreset = eqPreset;
        model.setEqPreset(eqPreset);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        model.onClean();
    }
}

