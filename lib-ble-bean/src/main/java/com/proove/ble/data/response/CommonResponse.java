package com.proove.ble.data.response;

import com.google.gson.JsonObject;

public class CommonResponse {
    private int errorCode;
    private String message;
    private JsonObject data;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CommonResponse{" +
                "code=" + errorCode +
                ", msg='" + message + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
