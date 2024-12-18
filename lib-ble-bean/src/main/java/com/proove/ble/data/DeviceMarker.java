package com.proove.ble.data;

public class DeviceMarker {
    private String deviceName;
    private String mac;
    private double latitude;
    private double longitude;
    private int imageResId;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    @Override
    public String toString() {
        return "DeviceMarker{" +
                "deviceName='" + deviceName + '\'' +
                ", mac='" + mac + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", imageResId=" + imageResId +
                '}';
    }
}
