package com.proove.smart.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.proove.ble.constant.BleServiceType;
import com.proove.ble.data.BluetoothDataParser;
import com.proove.ble.data.DeviceInfo;
import com.proove.smart.manager.BleManager;
import com.proove.smart.manager.DeviceManager;
import com.yscoco.lib.util.ByteUtil;

public class DeviceModelImpl extends BleManager.IBleEventListener implements Model {
    private final static String TAG = "DeviceModelImpl";
    private final BluetoothDataParser dataParser;
    private IDataCallback<BluetoothDataParser.DeviceStatus> deviceStatusIDataCallback;
    private IDataCallback<Boolean> connectStatusCallback;
    @Override
    public void setDeviceStatusIDataCallback(IDataCallback<BluetoothDataParser.DeviceStatus> deviceStatusIDataCallback) {
        this.deviceStatusIDataCallback = deviceStatusIDataCallback;
    }

    @Override
    public void setConnectStatusListener(IDataCallback<Boolean> callback) {
        this.connectStatusCallback = callback;
    }

    public DeviceModelImpl() {
        dataParser = new BluetoothDataParser();
        BleManager.getInstance().addBleEventListener(this);
    }


    @Override
    public void onBleConnectionChange(BluetoothDevice device, int state) {
        super.onBleConnectionChange(device, state);
        if (!device.getAddress().equals(DeviceManager.getInstance().getCurrentMac())) {
            return;
        }
        if (state == BluetoothProfile.STATE_DISCONNECTED) {
            DeviceInfo currentDeviceInfo = DeviceManager.getInstance().getCurrentDeviceInfo();
            currentDeviceInfo.setConnected(false);
        }
        if (connectStatusCallback == null) {
            return;
        }
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED -> {
                connectStatusCallback.onResult(true);
            }
            case BluetoothProfile.STATE_DISCONNECTED -> {
                connectStatusCallback.onResult(false);
            }
        }

    }

    @Override
    public void onBleDataNotify(BluetoothDevice device, BleServiceType bleServiceType, byte[] receivedData) {
        super.onBleDataNotify(device, bleServiceType, receivedData);
        onCharacteristicChanged(receivedData);
    }

    // 在收到蓝牙数据时调用
    public void onCharacteristicChanged(byte[] value) {
        if (deviceStatusIDataCallback != null) {
            dataParser.setCallback(status -> deviceStatusIDataCallback.onResult(status));
            dataParser.onDataReceived(value);
        } else {
            // 即使没有回调，也可以更新数据解析器的状态
            dataParser.onDataReceived(value);
        }
    }
}
