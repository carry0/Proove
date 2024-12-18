package com.proove.smart.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.proove.ble.data.SimpleLocation;
import com.yscoco.lib.util.ContextUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.OrientationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * LocationHelper
 */
public class LocationManager {
    private static final String TAG = "LocationManager";
    private static final long INTERVAL_MILLIS = 2000;
    private static final long DEFAULT_TIMEOUT = 15000; // 默认15秒超时
    private static final int MAX_RETRY_COUNT = 3;      // 最大重试次数
    private static final long RETRY_INTERVAL = 2000;   // 重试间隔2秒
    private static final long MIN_INTERVAL = 1000; // 最小间隔1秒

    // 添加新的成员变量
    private List<SimpleLocation> locationTrack = new ArrayList<>();
    private boolean isTrackingEnabled = false;
    private static final int MAX_TRACK_POINTS = 1000; // 最大轨迹点数量



    private FusedLocationProviderClient fusedLocationClient;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Set<ILocationListener> listeners = new CopyOnWriteArraySet<>();
    private final OrientationUtil.OnOrientationListener mOrientationListener = this::notifyDirect;
    
    private volatile boolean isRequesting = false;
    private volatile boolean isTimeout = false;
    private int retryCount = 0;
    private long startRequestTime = 0;
    private LocationRequest locationRequest;

    private final WorkManager workManager;
    private UUID timeoutWorkId;

    // 超时任务
    private final Runnable timeoutTask = () -> {
        if (isRequesting && !isTimeout) {
            isTimeout = true;
            LogUtil.error(TAG, "Location request timeout");
            handleTimeout();
        }
    };
    // 添加轨迹相关方法
    public void startTracking() {
        isTrackingEnabled = true;
        locationTrack.clear();

        // 修改定位请求为持续更新
        locationRequest = new LocationRequest.Builder(INTERVAL_MILLIS)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdates(Integer.MAX_VALUE)
                .build();

        startLocationUpdates();
    }

    public void stopTracking() {
        isTrackingEnabled = false;
        stopLocationUpdates();
    }
    private void addTrackPoint(Location location) {
        SimpleLocation trackPoint = locationToAddress(location);
        if (trackPoint != null) {
            if (locationTrack.size() >= MAX_TRACK_POINTS) {
                locationTrack.remove(0);
            }
            locationTrack.add(trackPoint);
            notifyTrackUpdate(locationTrack);
        }
    }
    public List<SimpleLocation> getLocationTrack() {
        return new ArrayList<>(locationTrack);
    }

    // 添加轨迹更新通知
    private void notifyTrackUpdate(List<SimpleLocation> track) {
        for (ILocationListener listener : listeners) {
            if (listener != null) {
                try {
                    listener.onTrackUpdate(new ArrayList<>(track));
                } catch (Exception e) {
                    LogUtil.error(TAG, "Error notifying track update: " + e.getMessage());
                }
            }
        }
    }
    // 位置回调
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location lastLocation = locationResult.getLastLocation();
            if (lastLocation != null) {
                handleLocationSuccess(lastLocation);
                if (isTrackingEnabled) {
                    addTrackPoint(lastLocation);
                }
            }
        }
    };

    private void handleLocationSuccess(Location location) {
        mainHandler.removeCallbacks(timeoutTask);
        isRequesting = false;
        isTimeout = false;
        retryCount = 0;
        
        SimpleLocation simpleLocation = locationToAddress(location);
        if (simpleLocation != null) {
            notifyUpdateLocation(simpleLocation);
        }
        
        // 成功获取位置后停止更新
        stopLocationUpdates();
    }

    // 处理超时情况
    private void handleTimeout() {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            LogUtil.info(TAG, "Retry getting location, attempt: " + retryCount);
            
            stopLocationUpdates();
            
            // 使用 WorkManager 进行重试
            Data inputData = new Data.Builder()
                    .putInt("retry_count", retryCount)
                    .build();

            OneTimeWorkRequest retryWork = new OneTimeWorkRequest.Builder(LocationRetryWorker.class)
                    .setInputData(inputData)
                    .setInitialDelay(RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                    .build();

            workManager.enqueue(retryWork);
        } else {
            LogUtil.error(TAG, "Max retry count reached");
            notifyError("Location request failed after " + MAX_RETRY_COUNT + " attempts");
            stopLocation();
        }
    }

    public void startLocation(ILocationListener listener, long timeout) {
        if (isRequesting) {
            LogUtil.info(TAG, "Location request already in progress");
            return;
        }

        if (listener == null || !checkPermissions()) {
            return;
        }

        addLocationListener(listener);
        
        isRequesting = true;
        isTimeout = false;
        retryCount = 0;
        startRequestTime = System.currentTimeMillis();

        initLocationClient();
        startLocationUpdates();

        // 使用 WorkManager 设置超时
        long timeoutDuration = Math.max(timeout > 0 ? timeout : DEFAULT_TIMEOUT, MIN_INTERVAL);
        scheduleTimeout(timeoutDuration);
        
        // 设置监听器自动移除
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            removeLocationListener(listener);
            stopLocation();
        }, timeoutDuration + MIN_INTERVAL);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(ContextUtil.getAppContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(ContextUtil.getAppContext(), 
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initLocationClient() {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(ContextUtil.getAppContext());
        }
        
        if (locationRequest == null) {
            locationRequest = new LocationRequest.Builder(INTERVAL_MILLIS)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdates(1) // 只获取一次位置
                    .build();
        }
    }

    private void startLocationUpdates() {
        if (!checkPermissions() || fusedLocationClient == null) {
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            LogUtil.error(TAG, "Security exception: " + e.getMessage());
            notifyError("Location permission denied");
            stopLocation();
        } catch (Exception e) {
            LogUtil.error(TAG, "Request location updates failed: " + e.getMessage());
            notifyError("Failed to start location updates");
            stopLocation();
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public void stopLocation() {
        isRequesting = false;
        isTimeout = false;
        cancelTimeout();
        stopLocationUpdates();
        retryCount = 0;
        startRequestTime = 0;
    }

    // 添加错误通知
    private void notifyError(String errorMessage) {
        for (ILocationListener listener : listeners) {
            if(listener != null) {
                try {
                    listener.onLocationError(errorMessage);
                } catch (Exception e) {
                    LogUtil.error(TAG, "Error notifying listener: " + e.getMessage());
                }
            }
        }
    }

    // 修改监听器接口，添加错误回调
    public abstract static class ILocationListener {
        public void onLastLocation(SimpleLocation simpleLocation) {}
        public void onUpdateLocation(SimpleLocation simpleLocation) {}
        public void onDirectChange(float direct) {}
        public void onLocationError(String errorMessage) {}
        public void onTrackUpdate(List<SimpleLocation> track) {}
    }

    private LocationManager() {
        workManager = WorkManager.getInstance(ContextUtil.getAppContext());
    }

    public static LocationManager getInstance() {
        return Singleton.instance;
    }

    private static final class Singleton {
        private static final LocationManager instance = new LocationManager();
    }

    /**
     * 开始定位
     */
    public void getLastLocation(ILocationListener listener) {
        if (listener == null || !checkPermissions()) {
            return;
        }
        addLocationListener(listener);
        
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(ContextUtil.getAppContext());
        }

        try {
            Task<Location> task = fusedLocationClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null) {
                    SimpleLocation simpleLocation = locationToAddress(location);
                    if (simpleLocation != null) {
                        notifyLastLocation(simpleLocation);
                    }
                }
            });
        } catch (SecurityException e) {
            LogUtil.error(TAG, "Security exception: " + e.getMessage());
            notifyError("Location permission denied");
        } catch (Exception e) {
            LogUtil.error(TAG, "Get last location failed: " + e.getMessage());
            notifyError("Failed to get last location");
        }
    }

    public void startLocation(ILocationListener listener) {
        // 使用默认超时调用带超时参数的版本
        startLocation(listener, DEFAULT_TIMEOUT);
    }

    private SimpleLocation locationToAddress(Location result) {
        if (result == null) return null;
        
        Geocoder gcd = new Geocoder(ContextUtil.getAppContext(), Locale.getDefault());
        try {
            List<Address> addressList = gcd.getFromLocation(result.getLatitude(), result.getLongitude(), 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                if (address != null) {
                    String addressStr = address.getAddressLine(0);
                    SimpleLocation simpleLocation = new SimpleLocation(addressStr,
                            address.getLongitude(),
                            address.getLatitude());
                    LogUtil.info(TAG, "address " + simpleLocation);
                    return simpleLocation;
                }
            }
        } catch (Exception e) {
            LogUtil.error(TAG, "get location Exception " + e.getMessage());
        }
        return null;
    }

    public void addLocationListener(ILocationListener listener) {
        if (listener == null || listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    public void removeLocationListener(ILocationListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    private void notifyUpdateLocation(SimpleLocation simpleLocation) {
        for (ILocationListener listener : listeners) {
            if (listener != null) {
                try {
                    listener.onUpdateLocation(simpleLocation);
                } catch (Exception e) {
                    LogUtil.error(TAG, "Error notifying update location: " + e.getMessage());
                }
            }
        }
    }

    private void notifyLastLocation(SimpleLocation simpleLocation) {
        for (ILocationListener listener : listeners) {
            if (listener != null) {
                try {
                    listener.onLastLocation(simpleLocation);
                } catch (Exception e) {
                    LogUtil.error(TAG, "Error notifying last location: " + e.getMessage());
                }
            }
        }
    }

    private void notifyDirect(float direct) {
        for (ILocationListener listener : listeners) {
            if (listener != null) {
                try {
                    listener.onDirectChange(direct);
                } catch (Exception e) {
                    LogUtil.error(TAG, "Error notifying direction change: " + e.getMessage());
                }
            }
        }
    }

    private void scheduleTimeout(long timeout) {
        cancelTimeout();
        
        Data inputData = new Data.Builder()
                .putLong("timeout_duration", timeout)
                .build();

        OneTimeWorkRequest timeoutWork = new OneTimeWorkRequest.Builder(LocationTimeoutWorker.class)
                .setInputData(inputData)
                .setInitialDelay(timeout, TimeUnit.MILLISECONDS)
                .build();

        timeoutWorkId = timeoutWork.getId();
        workManager.enqueue(timeoutWork);
    }

    private void cancelTimeout() {
        if (timeoutWorkId != null) {
            workManager.cancelWorkById(timeoutWorkId);
            timeoutWorkId = null;
        }
    }

    // 创建 Worker 类处理超时
    public static class LocationTimeoutWorker extends Worker {
        public LocationTimeoutWorker(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public Result doWork() {
            LocationManager.getInstance().handleTimeout();
            return Result.success();
        }
    }

    // 重试 Worker
    public static class LocationRetryWorker extends Worker {
        public LocationRetryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public Result doWork() {
            LocationManager instance = LocationManager.getInstance();
            if (instance.isRequesting) {
                instance.isTimeout = false;
                instance.startLocationUpdates();
            }
            return Result.success();
        }
    }
}
