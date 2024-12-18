package com.proove.smart.model;


import android.bluetooth.BluetoothDevice;

import com.jieli.bluetooth.bean.base.BaseError;
import com.jieli.bluetooth.constant.Constants;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.rcsp.callback.OnRcspActionCallback;
import com.proove.ble.constant.DeviceFindState;
import com.proove.ble.constant.Product;
import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.data.DeviceFindInfo;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.manager.MediaPlayManager;
import com.yscoco.lib.util.LogUtil;

public class DeviceEarFindModelImpl implements DeviceEarFindModel {
    private static final String TAG = "DeviceFindModelJLImpl";
    private static final int SEARCH_TIMEOUT = 60;

    private IDataCallback<DeviceFindInfo> deviceFindInfoCallback;

    @Override
    public void onClean() {
        MediaPlayManager.getInstance().stop();
    }

    @Override
    public void getDeviceFindInfo() {

    }

    @Override
    public void setDeviceFindInfoListener(IDataCallback<DeviceFindInfo> callback) {
        deviceFindInfoCallback = callback;
    }

    @Override
    public void setDeviceFindRing(DeviceSide side, DeviceFindState state, IResultCallback resultCallback) {
        Product currentProduct = DeviceManager.getInstance().getCurrentProduct();
        if (currentProduct == null) {
            return;
        }
        if (state == DeviceFindState.RINGING) {
            MediaPlayManager.getInstance().play(true);
        } else {
            MediaPlayManager.getInstance().pause();
        }
        resultCallback.onSuccess();
        if (deviceFindInfoCallback != null) {
            deviceFindInfoCallback.onResult(
                    new DeviceFindInfo(new byte[]{(byte) state.getValue(),
                            (byte) state.getValue()}));
        }
        RCSPController controller = RCSPController.getInstance();
        controller.searchDev(controller.getUsingDevice(), state.getValue(),
                SEARCH_TIMEOUT, DeviceSide.ALL.getValue(), Constants.RING_PLAYER_DEVICE,
                new OnRcspActionCallback<>() {
                    @Override
                    public void onSuccess(BluetoothDevice device, Boolean message) {
                        LogUtil.info(TAG, "setDeviceFindRing onSuccess");
                        resultCallback.onSuccess();
                        if (deviceFindInfoCallback != null) {
                            deviceFindInfoCallback.onResult(
                                    new DeviceFindInfo(new byte[]{(byte) state.getValue(),
                                            (byte) state.getValue()}));
                        }
                    }

                    @Override
                    public void onError(BluetoothDevice device, BaseError error) {
                        LogUtil.info(TAG, "setDeviceFindRing onError");
                        resultCallback.onFail();
                    }
                });
    }

    @Override
    public boolean isPlaying() {
        return MediaPlayManager.getInstance().isPlay();
    }
}
