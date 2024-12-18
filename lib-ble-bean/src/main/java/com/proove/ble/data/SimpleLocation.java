package com.proove.ble.data;

public class SimpleLocation {
    private String addressStr;
    private double longitude;
    private double latitude;

    public SimpleLocation() {
    }

    public SimpleLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public SimpleLocation(String addressStr, double longitude, double latitude) {
        this.addressStr = addressStr;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getAddressStr() {
        return addressStr;
    }

    public void setAddressStr(String addressStr) {
        this.addressStr = addressStr;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "SimpleLocation{" +
                "addressStr='" + addressStr + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
