package com.proove.ble.data.response;

public class FirmwareFile {
    String type;
    String url;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "FirmwareFile{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
