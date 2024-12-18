package com.proove.smart.model;

import android.bluetooth.BluetoothDevice;

import com.jieli.bluetooth.bean.base.BaseError;
import com.jieli.bluetooth.bean.response.ADVInfoResponse;
import com.jieli.bluetooth.constant.AttrAndFunCode;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.rcsp.callback.OnRcspActionCallback;
import com.proove.ble.constant.Product;
import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.constant.protocol.UiAction;
import com.proove.ble.constant.protocol.UiFunction;
import com.proove.ble.data.KeySettingsData;
import com.proove.ble.data.UiInfo;
import com.proove.smart.manager.DeviceManager;
import com.yscoco.lib.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class DeviceEarKeySettingModelImpl implements DeviceEarKeySettingModel {
    private final String TAG = "KeySettingModelImpl";

    private final RCSPController controller = RCSPController.getInstance();
    private IDataListener<UiInfo> uiInfoListener;

    @Override
    public void onClean() {
    }

    @Override
    public void getUiInfo() {
        controller.getDeviceSettingsInfo(controller.getUsingDevice(),
                0x01 << AttrAndFunCode.ADV_TYPE_KEY_SETTINGS, new OnRcspActionCallback<>() {
                    @Override
                    public void onSuccess(BluetoothDevice bluetoothDevice, ADVInfoResponse advInfoResponse) {
                        dealUIInfo(advInfoResponse);
                    }

                    @Override
                    public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {

                    }
                });
    }

    @Override
    public void setUiInfoListener(IDataListener<UiInfo> listener) {
        uiInfoListener = listener;
    }

    @Override
    public void setUi(DeviceSide side, UiAction action, UiFunction function) {
        List<ADVInfoResponse.KeySettings> keySettingsList = new ArrayList<>();
        ADVInfoResponse.KeySettings keySettings = new ADVInfoResponse.KeySettings();
        keySettings.setKeyNum(side.getValue());
        keySettings.setAction(action.getValue());
        keySettings.setFunction(function.getValue());
        keySettingsList.add(keySettings);
        setUi(keySettingsList);
    }

    @Override
    public void resetUi(DeviceSide side) {
        Product product = DeviceManager.getInstance().getCurrentProduct();
        if (product == null) {
            return;
        }
        List<UiAction> supportUiAction = product.getUiActions();
        List<UiFunction> supportUiFunction = product.getUiFunction();
        List<KeySettingsData> resetList = product.getResetUi();
        List<ADVInfoResponse.KeySettings> keySettingsList = new ArrayList<>();
        for (KeySettingsData keySettingsData : resetList) {
            if (keySettingsData.getDeviceSide() == side
                    && supportUiAction.contains(keySettingsData.getUiAction())
                    && supportUiFunction.contains(keySettingsData.getUiFunction())) {
                ADVInfoResponse.KeySettings keySettings = new ADVInfoResponse.KeySettings();
                keySettings.setKeyNum(side.getValue());
                keySettings.setAction(keySettingsData.getUiAction().getValue());
                keySettings.setFunction(keySettingsData.getUiFunction().getValue());
                keySettingsList.add(keySettings);
            }
        }
        setUi(keySettingsList);
    }

    private void setUi(List<ADVInfoResponse.KeySettings> keySettingsList) {
        LogUtil.info(TAG, "setUI = " + keySettingsList);
        RCSPController.getInstance().configKeySettings(controller.getUsingDevice(), keySettingsList,
                new OnRcspActionCallback<>() {
                    @Override
                    public void onSuccess(BluetoothDevice bluetoothDevice, Integer integer) {
                        LogUtil.info(TAG, "setUI onSuccess " + integer);
                        getUiInfo();
                    }

                    @Override
                    public void onError(BluetoothDevice bluetoothDevice, BaseError baseError) {
                        LogUtil.info(TAG, "setUI onError " + baseError);
                    }
                });

    }

    private void dealUIInfo(ADVInfoResponse advInfoResponse) {
        if (advInfoResponse == null) {
            return;
        }
        List<ADVInfoResponse.KeySettings> keySettingsList = advInfoResponse.getKeySettingsList();
        if (keySettingsList == null || keySettingsList.isEmpty()) {
            return;
        }
        LogUtil.info(TAG, keySettingsList.toString());
        UiInfo uiInfo = new UiInfo();
        Product product = DeviceManager.getInstance().getCurrentProduct();
        if (product == null) {
            return;
        }
        uiInfo.setUiActionList(product.getUiActions());
        uiInfo.setUiFunctionList(product.getUiFunction());
        for (ADVInfoResponse.KeySettings keySettings : keySettingsList) {
            DeviceSide deviceSide = DeviceSide.valueOf(keySettings.getKeyNum());
            UiAction uiAction = UiAction.valueOf(keySettings.getAction());
            UiFunction uiFunction = UiFunction.valueOf(keySettings.getFunction());
            switch (deviceSide) {
                case LEFT -> {
                    switch (uiAction) {
                        case CLICK -> uiInfo.setLeftOneClickFunction(uiFunction);
                        case DOUBLE_CLICK -> uiInfo.setLeftTwoClickFunction(uiFunction);
                        case TRIPLE_HIT -> uiInfo.setLeftThreeClickFunction(uiFunction);
                        case LONG_PRESS -> uiInfo.setLeftLongClickFunction(uiFunction);
                    }
                }
                case RIGHT -> {
                    switch (uiAction) {
                        case CLICK -> uiInfo.setRightOneClickFunction(uiFunction);
                        case DOUBLE_CLICK -> uiInfo.setRightTwoClickFunction(uiFunction);
                        case TRIPLE_HIT -> uiInfo.setRightThreeClickFunction(uiFunction);
                        case LONG_PRESS -> uiInfo.setRightLongClickFunction(uiFunction);
                    }
                }
            }
        }
        if (uiInfoListener != null) {
            uiInfoListener.onResult(uiInfo);
        }
    }
}
