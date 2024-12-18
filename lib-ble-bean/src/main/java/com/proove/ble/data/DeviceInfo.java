package com.proove.ble.data;


import androidx.annotation.Nullable;

import com.proove.ble.entity.DeviceInfoEntity;

public class DeviceInfo {
    private String mac;
    private String bleMac;
    private int brandId;
    private int productId;
    private String license;
    private String deviceName;
    private String productName;
    private boolean isConnected;
    private int imageResId;
    private int iconResId;
    private String location;
    private String longitude;
    private String latitude;
    private long lastRecordTime;
    private long latestConnectTime;

    public DeviceInfo() {
    }

    public DeviceInfo(DeviceInfoEntity deviceInfoEntity) {
        setLatestConnectTime(deviceInfoEntity.getLatestConnectTime());
        setMac(deviceInfoEntity.getMac());
        setBleMac(deviceInfoEntity.getBleMac());
        setProductId(deviceInfoEntity.getProductId());
        setDeviceName(deviceInfoEntity.getDeviceName());
        setLatitude(deviceInfoEntity.getLatitude());
        setLongitude(deviceInfoEntity.getLongitude());
        setLocation(deviceInfoEntity.getLocation());
        setLastRecordTime(deviceInfoEntity.getLastRecordTime());
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getBleMac() {
        return bleMac;
    }

    public void setBleMac(String bleMac) {
        this.bleMac = bleMac;
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

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public long getLastRecordTime() {
        return lastRecordTime;
    }

    public void setLastRecordTime(long lastRecordTime) {
        this.lastRecordTime = lastRecordTime;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public long getLatestConnectTime() {
        return latestConnectTime;
    }

    public void setLatestConnectTime(long latestConnectTime) {
        this.latestConnectTime = latestConnectTime;
    }

    public boolean equalsEarDevice() {
        return getProductId() == 0x0001 || getProductId() == 0x0002 || getProductId() == 0x0003 ||
                getProductId() == 0x0004 || getProductId() == 0x0005 || getProductId() == 0x0006 ||
                getProductId() == 0x0007 || getProductId() == 0x0008 || getProductId() == 0x0009;
    }

    public boolean equalsDevice() {
        return getProductId() == 0x0000B || getProductId() == 0x0000A || getProductId() == 0x0000C ||
                getProductId() == 0x0000D;
    }
    @Override
    public String toString() {
        return "DeviceInfo{" +
                "mac='" + mac + '\'' +
                ", bleMac='" + bleMac + '\'' +
                ", brandId=" + brandId +
                ", productId=" + productId +
                ", license='" + license + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", productName='" + productName + '\'' +
                ", isConnected=" + isConnected +
                ", imageResId=" + imageResId +
                ", iconResId=" + iconResId +
                ", location='" + location + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", lastRecordTime=" + lastRecordTime +
                ", latestConnectTime=" + latestConnectTime +
                '}';
    }
}
