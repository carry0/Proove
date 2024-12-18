package com.proove.ble.data;

public class OtaInfo {
    private String version;
    private String UpgradableVersion;
    private String firmwareFileUrl;
    private String firmwareUpgradeDesc;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpgradableVersion() {
        return UpgradableVersion;
    }

    public void setUpgradableVersion(String upgradableVersion) {
        UpgradableVersion = upgradableVersion;
    }

    public String getFirmwareFileUrl() {
        return firmwareFileUrl;
    }

    public void setFirmwareFileUrl(String firmwareFileUrl) {
        this.firmwareFileUrl = firmwareFileUrl;
    }

    public String getFirmwareUpgradeDesc() {
        return firmwareUpgradeDesc;
    }

    public void setFirmwareUpgradeDesc(String firmwareUpgradeDesc) {
        this.firmwareUpgradeDesc = firmwareUpgradeDesc;
    }

    @Override
    public String toString() {
        return "OtaInfo{" +
                "version='" + version + '\'' +
                ", UpgradableVersion='" + UpgradableVersion + '\'' +
                ", firmwareFileUrl='" + firmwareFileUrl + '\'' +
                ", firmwareUpgradeDesc='" + firmwareUpgradeDesc + '\'' +
                '}';
    }
}
