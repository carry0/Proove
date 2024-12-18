package com.proove.ble.data;

public class DeviceListItem {
    private int productId;
    private String mac;
    private String deviceName;
    private int productImageResId;
    private boolean isConnected;

    public DeviceInfo deviceInfo;
    public DeviceListItem() {}

    public DeviceListItem(int productId, String deviceName, int productImageResId) {
        this.productId = productId;
        this.deviceName = deviceName;
        this.productImageResId = productImageResId;
    }

    public DeviceListItem(DeviceInfo deviceInfo) {
        this.deviceInfo =deviceInfo;
        setMac(deviceInfo.getMac());
        setDeviceName(deviceInfo.getDeviceName());
        setConnected(deviceInfo.isConnected());
        setProductId(deviceInfo.getProductId());
        setProductImageResId(deviceInfo.getImageResId());
    }
    public long getAddedTime() {
        return deviceInfo.getLastRecordTime();
    }
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getProductImageResId() {
        return productImageResId;
    }

    public void setProductImageResId(int productImageResId) {
        this.productImageResId = productImageResId;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public boolean equalsEarDevice() {
        return getProductId() == 0x0001 || getProductId() == 0x0002 || getProductId() == 0x0003 ||
                getProductId() == 0x0004 || getProductId() == 0x0005 || getProductId() == 0x0006 ||
                getProductId() == 0x0007 || getProductId() == 0x0008 || getProductId() == 0x0009||
                getProductId() == 0x000A || getProductId() == 0x000B || getProductId() == 0x000C||
                getProductId() == 0x000D || getProductId() == 0x000E || getProductId() == 0x000F||
                getProductId() == 0x0000;
    }

    public boolean equalsDevice() {
        return getProductId() == 0xAAAA||getProductId() == 0xBBBB||getProductId() == 0xCCCC||getProductId() == 0xDDDD;
    }
}
