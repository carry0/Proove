package com.proove.ble.data;


import com.proove.ble.constant.Product;

public class DevicePopInfo {
    private Product product;
    private DeviceInfo deviceInfo;
    private boolean isShow;

    public DevicePopInfo() {
    }

    public DevicePopInfo(Product product, DeviceInfo deviceInfo) {
        this.product = product;
        this.deviceInfo = deviceInfo;
    }

    public DevicePopInfo(Product product, DeviceInfo deviceInfo, boolean isShow) {
        this.product = product;
        this.deviceInfo = deviceInfo;
        this.isShow = isShow;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
