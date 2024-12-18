package com.proove.smart.manager;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jieli.bluetooth.utils.BluetoothUtil;
import com.proove.ble.constant.Constant;
import com.yscoco.lib.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IBluetoothManager {
    private static final String TAG = "BluetoothManager";
    private static final long THROTTLE_INTERVAL = 100; // 100ms节流间隔
    private static final int BUFFER_PROCESS_INTERVAL = 50; // 50ms处理一次缓冲队列
    private static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    private static final long SCAN_PERIOD = 10000; // 扫描持续时间10秒

    private final Context context;
    private final Handler mainHandler;
    private final ExecutorService executor;
    private final Queue<BluetoothEvent> eventQueue;
    private final Map<String, WeakReference<BluetoothCallback>> callbackMap;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    public boolean isScanning = false;
    private long lastCallbackTime = 0;
    private volatile boolean isProcessingQueue = false;

    // 单例模式
    private static volatile IBluetoothManager instance;

    public static IBluetoothManager getInstance(Context context) {
        if (instance == null) {
            synchronized (IBluetoothManager.class) {
                if (instance == null) {
                    instance = new Builder(context).build();
                }
            }
        }
        return instance;
    }

    // Builder模式构建实例
    public static class Builder {
        private final Context context;
        private ExecutorService executor;
        private Handler handler;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder setExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder setHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public IBluetoothManager build() {
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor();
            }
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            return new IBluetoothManager(context, executor, handler);
        }
    }

    private IBluetoothManager(Context context, ExecutorService executor, Handler handler) {
        this.context = context.getApplicationContext();
        this.executor = executor;
        this.mainHandler = handler;
        this.eventQueue = new ConcurrentLinkedQueue<>();
        this.callbackMap = new ConcurrentHashMap<>();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerBluetoothReceiver();
        startQueueProcessor();
    }

    // 注册蓝牙广播接收器
    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(bluetoothReceiver, filter);
    }

    // 蓝牙广播接收器
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.DEVICE_FOUND)
                                .setDevice(device)
                                .build());
                    }
                    break;

                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.STATE_CHANGED)
                            .setState(state)
                            .build());
                    break;

                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    if (device != null) {
                        handleBondStateChanged(device, bondState);
                    }
                    break;
            }
        }
    };

    // 处理配对状态变化
    private void handleBondStateChanged(BluetoothDevice device, int bondState) {
        switch (bondState) {
            case BluetoothDevice.BOND_BONDED:
                handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.DEVICE_CONNECTED)
                        .setDevice(device)
                        .build());
                break;
            case BluetoothDevice.BOND_NONE:
                handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.DEVICE_DISCONNECTED)
                        .setDevice(device)
                        .build());
                break;
        }
    }

    // BLE扫描回调
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result == null || result.getDevice() == null || result.getScanRecord() == null)
                return;
            byte[] bleData = result.getScanRecord().getManufacturerSpecificData(Constant.COMPANY_ID);
            if (bleData == null) {
                return;
            }
            handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.DEVICE_FOUND)
                    .setDevice(result.getDevice())
                    .setScanResult(result)
                    .setData(bleData)  // 添加bleData到事件中
                    .build());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            LogUtil.error(TAG, "onScanFailed : " + errorCode);
            stopScan();
        }
    };

    // 开始扫描
    public void startScan() {
        if (!BluetoothUtil.isBluetoothEnable() || isScanning) {
            LogUtil.error(TAG, "Cannot start scan: Bluetooth disabled or already scanning");
            return;
        }

        handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.SCAN_START).build());

        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (bluetoothLeScanner != null) {
            isScanning = true;

            // 配置扫描设置
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            try {
                // 开始扫描
                bluetoothLeScanner.startScan(null, settings, scanCallback);
                LogUtil.info(TAG, "BLE scan started");

                // 设置扫描超时
                mainHandler.postDelayed(this::stopScan, SCAN_PERIOD);
            } catch (Exception e) {
                LogUtil.error(TAG, "Error starting scan: " + e.getMessage());
                isScanning = false;
            }
        } else {
            LogUtil.error(TAG, "BluetoothLeScanner is null");
        }
    }

    // 停止扫描
    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (!isScanning) return;

        isScanning = false;
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }

        handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.SCAN_STOP).build());
    }

    // 连接设备
    @SuppressLint("MissingPermission")
    public void connectDevice(BluetoothDevice device) {
        if (device == null) return;

        handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.DEVICE_CONNECTED)
                .setDevice(device)
                .build());
    }

    // 断开连接
    @SuppressLint("MissingPermission")
    public void disconnectDevice(BluetoothDevice device) {
        if (device == null) return;

        handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.DEVICE_DISCONNECTED)
                .setDevice(device)
                .build());
    }

    // 发送数据
    public void sendData(BluetoothDevice device, byte[] data) {
        if (device == null || data == null) return;

        executor.execute(() -> {
            // 实现发送数据的逻辑
            // ...

            // 发送完成后通知结果
            handleBluetoothEvent(new BluetoothEvent.Builder(BluetoothEvent.EventType.DATA_RECEIVED)
                    .setDevice(device)
                    .setData(data)
                    .build());
        });
    }

    // 注册回调
    public void registerCallback(String key, BluetoothCallback callback) {
        if (callback != null) {
            callbackMap.put(key, new WeakReference<>(callback));
        }
    }

    // 取消注册回调
    public void unregisterCallback(String key) {
        callbackMap.remove(key);
    }

    // 处理蓝牙事件
    public void handleBluetoothEvent(BluetoothEvent event) {
        if (event == null) return;

        // 节流控制
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCallbackTime < THROTTLE_INTERVAL) {
            eventQueue.offer(event);
            return;
        }
        lastCallbackTime = currentTime;

        processEvent(event);
    }

    // 启动队列处理器
    private void startQueueProcessor() {
        if (isProcessingQueue) return;

        isProcessingQueue = true;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isProcessingQueue) return;

                processEventQueue();
                mainHandler.postDelayed(this, BUFFER_PROCESS_INTERVAL);
            }
        });
    }

    // 处理事件队列
    private void processEventQueue() {
        List<BluetoothEvent> events = new ArrayList<>();
        BluetoothEvent event;
        while ((event = eventQueue.poll()) != null) {
            events.add(event);
        }

        if (!events.isEmpty()) {
            // 批量处理事件
            for (BluetoothEvent evt : events) {
                processEvent(evt);
            }
        }
    }

    // 处理单个事件
    private void processEvent(final BluetoothEvent event) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 在工作线程中处理事件
                    final BluetoothResult result = processEventInBackground(event);

                    // 在主线程中回调结果
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyCallbacks(result);
                        }
                    });
                } catch (Exception e) {
                    LogUtil.error(TAG, "Process event error: " + e.getMessage());
                }
            }
        });
    }

    // 后台处理事件
    private BluetoothResult processEventInBackground(BluetoothEvent event) {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                BluetoothResult result = doProcessEvent(event);
                if (result != null && result.isSuccess()) {
                    return result;
                }

                // 如果结果是某些特定类型的错误，直接返回不重试
                if (result != null && (
                        result.getType() == BluetoothResult.ResultType.BLUETOOTH_DISABLED ||
                                result.getType() == BluetoothResult.ResultType.PERMISSION_DENIED ||
                                result.getType() == BluetoothResult.ResultType.DEVICE_NOT_FOUND
                )) {
                    return result;
                }

                retryCount++;
                if (retryCount < MAX_RETRY_COUNT) {
                    Thread.sleep(100 * retryCount);
                }
            } catch (Exception e) {
                lastException = e;
                LogUtil.error(TAG, "Process event error (attempt " + (retryCount + 1) +
                        "/" + MAX_RETRY_COUNT + "): " + e.getMessage());
                retryCount++;
                if (retryCount < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(100 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return new BluetoothResult.Builder(BluetoothResult.ResultType.FAILED)
                .setSourceEvent(event)
                .setError(lastException)
                .setMessage("Failed after " + MAX_RETRY_COUNT + " attempts" +
                        (lastException != null ? ": " + lastException.getMessage() : ""))
                .build();
    }

    // 具体处理事件
    private BluetoothResult doProcessEvent(BluetoothEvent event) {
        try {
            switch (event.getType()) {
                case SCAN_START:
                    return handleScanStart(event);

                case SCAN_STOP:
                    Log.i(TAG, "SCAN_STOP: ");
                    return handleScanStop(event);

                case DEVICE_FOUND:
                    Log.i(TAG, "DEVICE_FOUND: ");
                    return handleDeviceFound(event);

                case DEVICE_CONNECTED:
                    Log.i(TAG, "DEVICE_CONNECTED: ");
                    return handleDeviceConnected(event);

                case DEVICE_DISCONNECTED:
                    Log.i(TAG, "DEVICE_DISCONNECTED: ");
                    return handleDeviceDisconnected(event);

                case DATA_RECEIVED:
                    Log.i(TAG, "DATA_RECEIVED: ");
                    return handleDataReceived(event);

                case STATE_CHANGED:
                    return handleStateChanged(event);

                default:
                    return new BluetoothResult.Builder(BluetoothResult.ResultType.UNKNOWN_ERROR)
                            .setSourceEvent(event)
                            .setMessage("Unknown event type: " + event.getType())
                            .build();
            }
        } catch (Exception e) {
            return new BluetoothResult.Builder(BluetoothResult.ResultType.FAILED)
                    .setSourceEvent(event)
                    .setError(e)
                    .setMessage("Process event failed: " + e.getMessage())
                    .build();
        }
    }

    // 处理开始扫描事件
    private BluetoothResult handleScanStart(BluetoothEvent event) {
        if (!BluetoothUtil.isBluetoothEnable()) {
            return new BluetoothResult.Builder(BluetoothResult.ResultType.BLUETOOTH_DISABLED)
                    .setSourceEvent(event)
                    .setMessage("Bluetooth is disabled")
                    .build();
        }

        startScan();

        return new BluetoothResult.Builder(BluetoothResult.ResultType.SUCCESS)
                .setSourceEvent(event)
                .setMessage("Scan started successfully")
                .build();
    }

    // 处理停止扫描事件
    private BluetoothResult handleScanStop(BluetoothEvent event) {
        stopScan();
        return new BluetoothResult.Builder(BluetoothResult.ResultType.SUCCESS)
                .setSourceEvent(event)
                .setMessage("Scan stopped successfully")
                .build();
    }

    // 处理发现设备事件
    private BluetoothResult handleDeviceFound(BluetoothEvent event) {
        BluetoothDevice device = event.getDevice();
        if (device == null) {
            return new BluetoothResult.Builder(BluetoothResult.ResultType.DEVICE_NOT_FOUND)
                    .setSourceEvent(event)
                    .setMessage("Device is null")
                    .build();
        }
        if (event.getData() == null) {
            return new BluetoothResult.Builder(BluetoothResult.ResultType.SUCCESS)
                    .setSourceEvent(event)
                    .setDevice(device)
                    .setMessage("Device found: " + device.getAddress())
                    .build();
        }
        return new BluetoothResult.Builder(BluetoothResult.ResultType.SUCCESS)
                .setSourceEvent(event)
                .setDevice(device)
                .setData(event.getData())
                .setMessage("Device found: " + device.getAddress()+"\t\tDevice Data: "+event.getData().length)
                .build();
    }

    // 处理设备连接事件
    private BluetoothResult handleDeviceConnected(BluetoothEvent event) {
        BluetoothDevice device = event.getDevice();
        if (device == null) {
            return new BluetoothResult.Builder(BluetoothResult.ResultType.DEVICE_NOT_FOUND)
                    .setSourceEvent(event)
                    .setMessage("Device is null")
                    .build();
        }

        return new BluetoothResult.Builder(BluetoothResult.ResultType.SUCCESS)
                .setSourceEvent(event)
                .setDevice(device)
                .setMessage("Device connected: " + device.getAddress())
                .build();
    }

    // 处理设备断开连接事件
    private BluetoothResult handleDeviceDisconnected(BluetoothEvent event) {
        BluetoothDevice device = event.getDevice();
        if (device == null) {
            return new BluetoothResult.Builder(BluetoothResult.ResultType.DEVICE_NOT_FOUND)
                    .setSourceEvent(event)
                    .setMessage("Device is null")
                    .build();
        }

        return new BluetoothResult.Builder(BluetoothResult.ResultType.SUCCESS)
                .setSourceEvent(event)
                .setDevice(device)
                .setMessage("Device disconnected: " + device.getAddress())
                .build();
    }

    // 处理数据接收事件
    private BluetoothResult handleDataReceived(BluetoothEvent event) {
        byte[] data = event.getData();
        if (data == null || data.length == 0) {
            return new BluetoothResult.Builder(BluetoothResult.ResultType.FAILED)
                    .setSourceEvent(event)
                    .setMessage("Received data is empty")
                    .build();
        }

        return new BluetoothResult.Builder(BluetoothResult.ResultType.SUCCESS)
                .setSourceEvent(event)
                .setDevice(event.getDevice())
                .setData(data)
                .setMessage("Data received successfully")
                .build();
    }

    // 处理状态变化事件
    private BluetoothResult handleStateChanged(BluetoothEvent event) {
        return new BluetoothResult.Builder(BluetoothResult.ResultType.SUCCESS)
                .setSourceEvent(event)
                .setState(event.getState())
                .setMessage("Bluetooth state changed: " + event.getState())
                .build();
    }

    // 通知所有回调
    private void notifyCallbacks(BluetoothResult result) {
        if (result == null) return;

        for (Map.Entry<String, WeakReference<BluetoothCallback>> entry : callbackMap.entrySet()) {
            BluetoothCallback callback = entry.getValue().get();
            if (callback != null) {
                callback.onBluetoothResult(result);
            } else {
                callbackMap.remove(entry.getKey());
            }
        }
    }

    // 释放资源
    public void release() {
        isProcessingQueue = false;
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdown();
        eventQueue.clear();
        callbackMap.clear();
        stopScan();
        try {
            context.unregisterReceiver(bluetoothReceiver);
        } catch (Exception e) {
            LogUtil.error(TAG, "Unregister receiver error: " + e.getMessage());
        }
    }

    // 回调接口
    public interface BluetoothCallback {
        void onBluetoothResult(BluetoothResult result);
    }
}



