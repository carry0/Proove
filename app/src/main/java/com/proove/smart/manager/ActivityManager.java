package com.proove.smart.manager;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ActivityManager implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "ActivityManager";

    private final List<Activity> activityList = new ArrayList<>();

    private ActivityManager() {
    }

    public static ActivityManager getInstance() {
        return Singleton.instance;
    }

    private static final class Singleton {
        private static final ActivityManager instance = new ActivityManager();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        activityList.add(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        activityList.remove(activity);
    }

    public void recreate() {
        for (Activity activity : activityList) {
            activity.recreate();
        }
    }

    public void finishActivity(Class<? extends Activity> activityClass) {
        for (Activity activity : activityList) {
            if (activityClass.isInstance(activity)) {
                activity.finish();
            }
        }
    }
}
