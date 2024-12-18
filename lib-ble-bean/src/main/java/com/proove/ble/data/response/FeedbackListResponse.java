package com.proove.ble.data.response;

import java.util.List;

public class FeedbackListResponse {

    private int errorCode;
    private String message;
    private List<FeedbackData> data;

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setData(List<FeedbackData> data) {
        this.data = data;
    }

    public List<FeedbackData> getData() {
        return data;
    }

}