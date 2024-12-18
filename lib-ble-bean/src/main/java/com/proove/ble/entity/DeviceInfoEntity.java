package com.proove.ble.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.proove.ble.data.DeviceInfo;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "device_info")
public class DeviceInfoEntity {
    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "mac")
    private String mac;
    @ColumnInfo(name = "ble_mac")
    private String bleMac;
    @ColumnInfo(name = "product_id")
    private int productId;
    @ColumnInfo(name = "device_name")
    private String deviceName;
    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "longitude")
    private String longitude;
    @ColumnInfo(name = "latitude")
    private String latitude;
    @ColumnInfo(name = "last_record_time")
    private long lastRecordTime;

    @ColumnInfo(name = "latest_connect_time")
    private long latestConnectTime;
    public DeviceInfoEntity() {}

    public DeviceInfoEntity(DeviceInfo deviceInfo) {
        setMac(deviceInfo.getMac());
        setBleMac(deviceInfo.getBleMac());
        setProductId(deviceInfo.getProductId());
        setDeviceName(deviceInfo.getDeviceName());
        setLocation(deviceInfo.getLocation());
        setLongitude(deviceInfo.getLongitude());
        setLatitude(deviceInfo.getLatitude());
        setLastRecordTime(deviceInfo.getLastRecordTime());
        if (this.latestConnectTime==0){
            setLatestConnectTime(deviceInfo.getLatestConnectTime());
        }
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @NonNull
    public String getMac() {
        return mac;
    }

    public void setMac(@NonNull String mac) {
        this.mac = mac;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getBleMac() {
        return bleMac;
    }

    public void setBleMac(String bleMac) {
        this.bleMac = bleMac;
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

    public long getLatestConnectTime() {
        return latestConnectTime;
    }

    public void setLatestConnectTime(long latestConnectTime) {
        this.latestConnectTime = latestConnectTime;
    }
}
