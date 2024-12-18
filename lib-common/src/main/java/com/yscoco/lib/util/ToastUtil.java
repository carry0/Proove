package com.yscoco.lib.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtil {

    private static Toast sToast;

    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message, int duration) {
        if (sToast != null) {
            sToast.cancel();
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            sToast = Toast.makeText(context, message, duration);
            sToast.show();
        });
    }
}
