package com.proove.smart.util;

public class FastClickUtil {
    private static final int MIN_DELAY_TIME= 1000; // 两次点击间隔不能少于1000ms
    private static long lastClickTime;

    public static boolean isFastClick() {
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) > MIN_DELAY_TIME) {
            return false;
        }
        lastClickTime = currentClickTime;
        return true;
    }
}
