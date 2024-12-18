package com.proove.ble.data.response;

import java.util.List;

public class ProductInfo {

    private String id;
    private String pid;
    private String name;
    private List<String> image;
    private String cateId;
    private String desc;
    private String firmwareVersion;
    private String firmwarePath;
    private String firmwareUpdateDesc;
    private String instructionsPage;
    private String firmwareNum;
    private List<FirmwareFile> firmwareList;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImage(List<String> image) {
        this.image = image;
    }

    public List<String> getImage() {
        return image;
    }

    public void setCateId(String cateId) {
        this.cateId = cateId;
    }

    public String getCateId() {
        return cateId;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwarePath(String firmwarePath) {
        this.firmwarePath = firmwarePath;
    }

    public String getFirmwarePath() {
        return firmwarePath;
    }

    public void setFirmwareUpdateDesc(String firmwareUpdateDesc) {
        this.firmwareUpdateDesc = firmwareUpdateDesc;
    }

    public String getFirmwareUpdateDesc() {
        return firmwareUpdateDesc;
    }

    public void setInstructionsPage(String instructionsPage) {
        this.instructionsPage = instructionsPage;
    }

    public String getInstructionsPage() {
        return instructionsPage;
    }

    public String getFirmwareNum() {
        return firmwareNum;
    }

    public void setFirmwareNum(String firmwareNum) {
        this.firmwareNum = firmwareNum;
    }

    public List<FirmwareFile> getFirmwareList() {
        return firmwareList;
    }

    public void setFirmwareList(List<FirmwareFile> firmwareList) {
        this.firmwareList = firmwareList;
    }

    @Override
    public String toString() {
        return "ProductInfo{" +
                "id='" + id + '\'' +
                ", pid='" + pid + '\'' +
                ", name='" + name + '\'' +
                ", image=" + image +
                ", cateId='" + cateId + '\'' +
                ", desc='" + desc + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", firmwarePath='" + firmwarePath + '\'' +
                ", firmwareUpdateDesc='" + firmwareUpdateDesc + '\'' +
                ", instructionsPage='" + instructionsPage + '\'' +
                ", firmwareNum='" + firmwareNum + '\'' +
                ", firmwareList=" + firmwareList +
                '}';
    }
}
