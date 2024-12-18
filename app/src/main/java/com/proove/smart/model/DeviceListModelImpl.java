package com.proove.smart.model;

import android.bluetooth.BluetoothDevice;

import com.jieli.bluetooth.bean.base.BaseError;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.rcsp.callback.OnRcspActionCallback;
import com.jieli.bluetooth.tool.DeviceAddrManager;
import com.proove.ble.constant.DeviceInfoType;
import com.proove.ble.data.DeviceInfo;
import com.proove.smart.manager.DeviceManager;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.LogUtil;

import java.util.List;

public class DeviceListModelImpl implements DeviceListModel, DeviceManager.IDeviceEventListener {
    private static final String TAG = "DeviceListModelImpl";

    private IDataCallback<Boolean> connectStateCallback;
    private IDeviceListCallBack deviceChangeCallBack;
    private IConnectCallBack connectCallBack;
    private String connectingDevice = "";

    public DeviceListModelImpl() {
        DeviceManager.getInstance().addDeviceEventListener(this);
    }

    @Override
    public void setConnectStatusListener(IDataCallback<Boolean> callback) {
        connectStateCallback = callback;
    }

    @Override
    public void getDeviceInfoList(IDeviceListCallBack callBack) {
        this.deviceChangeCallBack = callBack;
        if (deviceChangeCallBack != null) {
            deviceChangeCallBack.onResult(DeviceManager.getInstance().getDeviceInfoList());
        }
    }
    @Override
    public void getConnectStatus() {
        boolean isConnected = RCSPController.getInstance().isDeviceConnected(BluetoothUtil
                .getBluetoothDevice(DeviceManager.getInstance().getCurrentMac()));
        LogUtil.info(TAG, "getConnectStatus = " + isConnected);
        notifyConnectState(isConnected);
    }

    private void notifyConnectState(boolean isConnected) {
        if (connectStateCallback == null) {
            return;
        }
        LogUtil.info(TAG, "notifyConnectState " + isConnected);
        connectStateCallback.onResult(isConnected);
    }

    @Override
    public void connectDevice(String mac, IConnectCallBack callBack) {
        this.connectCallBack = callBack;
        connectingDevice = mac;
        RCSPController controller = RCSPController.getInstance();
        BluetoothDevice usingDevice = controller.getUsingDevice();
        if (usingDevice != null && !usingDevice.getAddress().equals(mac)) {
            controller.disconnectDevice(usingDevice);
        }
        DeviceInfo deviceInfo = DeviceManager.getInstance().getDeviceInfo(mac);
        LogUtil.info(TAG, "mac = " + mac);
        LogUtil.info(TAG, "bleMac = " + deviceInfo.getBleMac());
        BluetoothDevice device = BluetoothUtil.getBluetoothDevice(mac);
        controller.connectDevice(device);
    }

    @Override
    public void deleteDevice(String mac) {
        DeviceManager.getInstance().removeDevice(mac);
        getDeviceInfoList(deviceChangeCallBack);
        RCSPController.getInstance().disconnectDevice(BluetoothUtil.getBluetoothDevice(mac));
        DeviceAddrManager.getInstance().removeHistoryBluetoothDevice(mac);
    }

    @Override
    public void onClean() {
        DeviceManager.getInstance().removeDeviceEventListener(this);
    }

    @Override
    public void onDeviceInfoChange(DeviceInfoType type, DeviceInfo deviceInfo) {
        if (deviceChangeCallBack != null) {
            deviceChangeCallBack.onResult(DeviceManager.getInstance().getDeviceInfoList());
        }
    }

    @Override
    public void onConnectionChange(DeviceInfo device, int state) {
        if (deviceChangeCallBack != null) {
            deviceChangeCallBack.onResult(DeviceManager.getInstance().getDeviceInfoList());
        }
    }

    @Override
    public void onDeviceListChange(List<DeviceInfo> deviceInfoList) {
        if (deviceChangeCallBack != null) {
            deviceChangeCallBack.onResult(deviceInfoList);
        }
    }
}
