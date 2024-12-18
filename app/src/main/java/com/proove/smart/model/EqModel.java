package com.proove.smart.model;


import com.proove.ble.constant.CommonEqInfo;
import com.proove.ble.constant.EqPreset;
import com.proove.ble.entity.EqInfoEntity;

import java.util.List;

public interface EqModel {
    interface IDataListener<T> {
        void onResult(T data);
    }

    interface IEqInfoEntityCallback {
        void onResult(EqInfoEntity data);
        void onFail();
    }
    interface IEqNameCallback {
        void onResult(List<String> data);
        void onFail();
    }
    void onClean();

    void getEqInfo();

    void setEqInfoListener(IDataListener<CommonEqInfo> listener);

    void setEq(int index, int[] freq, float[] gain);

    void setEqPreset(EqPreset eqPreset);

    void setEqCustom(int index);

    void saveEqData(int index, int[] freq, float[] gain);

    void editEQName(int index,String EQName);

    void getEQName(IEqNameCallback callback);
}
