package com.proove.smart.manager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

/**
 * 蓝牙事件封装类
 */
public class BluetoothEvent {

    public enum EventType {
        SCAN_START,           // 开始扫描
        SCAN_STOP,           // 停止扫描
        DEVICE_FOUND,        // 发现设备
        DEVICE_CONNECTED,    // 设备已连接
        DEVICE_DISCONNECTED, // 设备断开连接
        CONNECTION_FAILED,   // 连接失败
        STATE_CHANGED,       // 状态改变
        DATA_RECEIVED        // 接收到数据
    }

    private final EventType type;
    private BluetoothDevice device;
    private ScanResult scanResult;
    private byte[] data;
    private int state;
    private String mac;
    private Exception error;

    private BluetoothEvent(Builder builder) {
        this.type = builder.type;
        this.device = builder.device;
        this.scanResult = builder.scanResult;
        this.data = builder.data;
        this.state = builder.state;
        this.mac = builder.mac;
        this.error = builder.error;
    }

    public EventType getType() {
        return type;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public byte[] getData() {
        return data;
    }

    public int getState() {
        return state;
    }

    public String getMac() {
        return mac;
    }

    public Exception getError() {
        return error;
    }

    public static class Builder {
        private final EventType type;
        private BluetoothDevice device;
        private ScanResult scanResult;
        private byte[] data;
        private int state;
        private String mac;
        private Exception error;

        public Builder(EventType type) {
            this.type = type;
        }

        public Builder setDevice(BluetoothDevice device) {
            this.device = device;
            return this;
        }

        public Builder setScanResult(ScanResult scanResult) {
            this.scanResult = scanResult;
            return this;
        }

        public Builder setData(byte[] data) {
            this.data = data;
            return this;
        }

        public Builder setState(int state) {
            this.state = state;
            return this;
        }

        public Builder setMac(String mac) {
            this.mac = mac;
            return this;
        }

        public Builder setError(Exception error) {
            this.error = error;
            return this;
        }

        public BluetoothEvent build() {
            return new BluetoothEvent(this);
        }
    }

    @Override
    public String toString() {
        return "BluetoothEvent{" +
                "type=" + type +
                ", device=" + (device != null ? device.getAddress() : "null") +
                ", state=" + state +
                ", mac='" + mac + '\'' +
                ", error=" + error +
                '}';
    }
}
