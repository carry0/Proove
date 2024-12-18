package com.proove.smart.manager;


import android.bluetooth.BluetoothDevice;

/**
 * 蓝牙操作结果封装类
 */
public class BluetoothResult {

    public enum ResultType {
        SUCCESS,            // 操作成功
        FAILED,            // 操作失败
        TIMEOUT,           // 操作超时
        DEVICE_NOT_FOUND,  // 设备未找到
        BLUETOOTH_DISABLED,// 蓝牙未启用
        PERMISSION_DENIED, // 权限被拒绝
        UNKNOWN_ERROR      // 未知错误
    }

    private final ResultType type;
    private final BluetoothEvent sourceEvent; // 源事件
    private final BluetoothDevice device;     // 相关设备
    private final byte[] data;                // 数据
    private final String message;             // 结果消息
    private final Throwable error;            // 错误信息
    private final long timestamp;             // 时间戳
    private final int state;                  // 添加状态字段

    private BluetoothResult(Builder builder) {
        this.type = builder.type;
        this.sourceEvent = builder.sourceEvent;
        this.device = builder.device;
        this.data = builder.data;
        this.message = builder.message;
        this.error = builder.error;
        this.state = builder.state;           // 初始化状态
        this.timestamp = System.currentTimeMillis();
    }

    public static class Builder {
        private final ResultType type;
        private BluetoothEvent sourceEvent;
        private BluetoothDevice device;
        private byte[] data;
        private String message;
        private Throwable error;
        private int state;                    // 添加状态字段

        public Builder(ResultType type) {
            this.type = type;
        }

        public Builder setSourceEvent(BluetoothEvent event) {
            this.sourceEvent = event;
            return this;
        }

        public Builder setDevice(BluetoothDevice device) {
            this.device = device;
            return this;
        }

        public Builder setData(byte[] data) {
            this.data = data;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setError(Throwable error) {
            this.error = error;
            return this;
        }

        public Builder setState(int state) {  // 添加设置状态的方法
            this.state = state;
            return this;
        }

        public BluetoothResult build() {
            return new BluetoothResult(this);
        }
    }

    public ResultType getType() {
        return type;
    }

    public BluetoothEvent getSourceEvent() {
        return sourceEvent;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public byte[] getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getState() {                   // 添加获取状态的方法
        return state;
    }

    public boolean isSuccess() {
        return type == ResultType.SUCCESS;
    }

    @Override
    public String toString() {
        return "BluetoothResult{" +
                "type=" + type +
                ", device=" + (device != null ? device.getAddress() : "null") +
                ", state=" + state +
                ", message='" + message + '\'' +
                ", error=" + (error != null ? error.getMessage() : "null") +
                ", timestamp=" + timestamp +
                '}';
    }
}