package com.proove.ble.data.response;

import java.util.List;

public class ActivityListResponse {
    private int errorCode;
    private String message;
    private List<ActivityData> data;

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

    public List<ActivityData> getData() {
        return data;
    }

    public void setData(List<ActivityData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ActivityListResponse{" +
                "errorCode=" + errorCode +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
