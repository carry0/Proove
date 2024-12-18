package com.proove.ble.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class HandlerUtil {
    private final Handler mainHandler;
    private final Handler threadHandler;
    private HandlerUtil() {
        mainHandler = new Handler(Looper.getMainLooper());
        HandlerThread handlerThread = new HandlerThread("handlerThread");
        handlerThread.start();
        threadHandler = new Handler(handlerThread.getLooper());
    }

    private static class HandlerHolder {
        private static final HandlerUtil instance = new HandlerUtil();
    }

    public static HandlerUtil getInstance() {
        return HandlerHolder.instance;
    }

    public void runDelayTaskOnMainThread(Runnable runnable, long delayTime) {
        mainHandler.postDelayed(runnable, delayTime);
    }

    public void runTaskOnMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    public void removeTaskOnMainThread(Runnable runnable) {
        mainHandler.removeCallbacks(runnable);
    }

    public void removeAllTaskOnMainThread() {
        mainHandler.removeCallbacksAndMessages(null);
    }

    public void runDelayTaskOnHandlerThread(Runnable runnable, long delayTime) {
        threadHandler.postDelayed(runnable, delayTime);
    }

    public void runTaskOnHandlerThread(Runnable runnable) {
        threadHandler.post(runnable);
    }

    public void removeTaskOnHandlerThread(Runnable runnable) {
        threadHandler.removeCallbacks(runnable);
    }

    public void removeAllTaskOnHandlerThread() {
        threadHandler.removeCallbacksAndMessages(null);
    }
}
