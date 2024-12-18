package com.yscoco.lib.util;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LicenseUtil {
    private static final String TAG = "LicenseUtil";
    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    private static final String LICENSE_URL = "http://112.74.200.150:9001/system/license/validate/%s/%s/%s";

    public static final int VALIDATE_PASS = 0;
    public static final int VALIDATE_SUCCESS = 200;
    public static final int VALIDATE_LINE = 1000;

    public static void validate(String bid, String pid, String license, IValidateCallback callback) {
        RequestBody body = RequestBody.create(JSON, "");
        Request request = new Request.Builder()
                .url(String.format(LICENSE_URL, bid, pid, license))
                .addHeader("sign", "123456")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.error(TAG, "validate onFailure " + e.getMessage());
                callback.onPass(VALIDATE_PASS);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                LogUtil.info(TAG, "validate onResponse = " + response);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        try {
                            String str = response.body().string();
                            ResponseBody responseBody = GsonUtil.fromJson(str, ResponseBody.class);
                            LogUtil.info(TAG, "validate body" + responseBody);
                            if (responseBody.getCode() < VALIDATE_LINE) {
                                callback.onPass(responseBody.getCode());
                            } else {
                                callback.onFail();
                            }
                        } catch (IOException e) {
                            LogUtil.error(TAG, "validate onFailure" + e.getMessage());
                            callback.onPass(VALIDATE_PASS);
                        }
                    } else {
                        callback.onPass(VALIDATE_PASS);
                    }
                } else {
                    LogUtil.error(TAG, "validate fail");
                    callback.onPass(VALIDATE_PASS);
                }
            }
        });
    }

    public interface IValidateCallback {
        void onPass(int code);

        void onFail();
    }

    public static class ResponseBody {
        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "ResponseBody{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }
}
