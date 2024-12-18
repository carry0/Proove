package com.proove.ble.data;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.proove.ble.constant.BleRequestType;
import com.yscoco.lib.util.ByteUtil;

public class BleRequest {
    private BleRequestType bleRequestType;
    private byte[] data;
    private BluetoothGattCharacteristic targetCharacteristic;
    private BluetoothGattDescriptor targetDescriptor;

    public BleRequest(BleRequestType bleRequestType, byte[] data,
                      BluetoothGattCharacteristic targetCharacteristic, BluetoothGattDescriptor targetDescriptor) {
        this.bleRequestType = bleRequestType;
        this.data = data;
        this.targetCharacteristic = targetCharacteristic;
        this.targetDescriptor = targetDescriptor;
    }

    public BleRequestType getBleRequestType() {
        return bleRequestType;
    }

    public void setBleRequestType(BleRequestType bleRequestType) {
        this.bleRequestType = bleRequestType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public BluetoothGattCharacteristic getTargetCharacteristic() {
        return targetCharacteristic;
    }

    public void setTargetCharacteristic(BluetoothGattCharacteristic targetCharacteristic) {
        this.targetCharacteristic = targetCharacteristic;
    }

    public BluetoothGattDescriptor getTargetDescriptor() {
        return targetDescriptor;
    }

    public void setTargetDescriptor(BluetoothGattDescriptor targetDescriptor) {
        this.targetDescriptor = targetDescriptor;
    }

    @Override
    public String toString() {
        return "BleRequest{" +
                "bleRequestType=" + bleRequestType +
                ", data=" + ByteUtil.bytesToHexString(data) +
                ", targetCharacteristic=" + (targetCharacteristic == null ? null : targetCharacteristic.getUuid()) +
                ", targetDescriptor=" + (targetDescriptor == null ? null : targetDescriptor.getUuid()) +
                '}';
    }
}
