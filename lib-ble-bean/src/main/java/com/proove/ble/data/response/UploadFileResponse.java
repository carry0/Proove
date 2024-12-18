package com.proove.ble.data.response;

public class UploadFileResponse {
    private int errorCode;
    private String message;
    private UploadFileData data;

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

    public UploadFileData getData() {
        return data;
    }

    public void setData(UploadFileData data) {
        this.data = data;
    }

    public static class UploadFileData {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return "UploadFileData{" +
                    "path='" + path + '\'' +
                    '}';
        }
    }
}
