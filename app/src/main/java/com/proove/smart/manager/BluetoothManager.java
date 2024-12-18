package com.proove.smart.manager;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.proove.ble.constant.Constant;
import com.proove.ble.data.BleAdvMsg;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BluetoothManager {
    private static final String TAG = "BluetoothManager";

    private static final int DEFAULT_VALUE_BLUETOOTH = 1000;
    private static final Set<IBluetoothEventListener> bluetoothEventListeners = new HashSet<>();
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    private BluetoothA2dp bluetoothA2dp;
    private BluetoothHeadset bluetoothHeadset;
    private final BluetoothReceiver bluetoothReceiver = new BluetoothReceiver();
    private boolean scanning;
    private final Handler handler = new Handler();
    private static final long SCAN_PERIOD = 20000;
    private final Runnable stopScanTask = this::stopLeScan;

    private final BluetoothProfile.ServiceListener a2dpServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            LogUtil.info(TAG, "onServiceConnected i = " + i + " profile =" + bluetoothProfile);
            bluetoothA2dp = (BluetoothA2dp) bluetoothProfile;
        }

        @Override
        public void onServiceDisconnected(int i) {
            LogUtil.info(TAG, "onServiceDisconnected i = " + i);
            bluetoothA2dp = null;
        }
    };

    private final BluetoothProfile.ServiceListener hfpServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            LogUtil.info(TAG, "onServiceConnected i = " + i + " profile =" + bluetoothProfile);
            bluetoothHeadset = (BluetoothHeadset) bluetoothProfile;
        }

        @Override
        public void onServiceDisconnected(int i) {
            LogUtil.info(TAG, "onServiceDisconnected i = " + i);
            bluetoothHeadset = null;
        }
    };

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i(TAG, "onScanResult: "+result.getDevice().getName());
            if (result == null || result.getDevice() == null || result.getScanRecord() == null) {
                return;
            }
            byte[] bleData = result.getScanRecord().getManufacturerSpecificData(Constant.COMPANY_ID);
            byte[] bleData2 = result.getScanRecord().getManufacturerSpecificData(Constant.ID);
            if (bleData2 != null) {
                BleAdvMsg bleAdvMsg = new BleAdvMsg();
                if (result.getDevice()!=null&&result.getDevice().getName()!=null){
                    if ( TextUtils.equals(result.getDevice().getName(),"Proove X-City Pro")){
                        bleAdvMsg.setPid(0xAAAA);
                    }else if (TextUtils.equals(result.getDevice().getName(),"Proove X-City Pro Max")){
                        bleAdvMsg.setPid(0xBBBB);
                    }else if (TextUtils.equals(result.getDevice().getName(),"Proove Dual Sport")){
                        bleAdvMsg.setPid(0xCCCC);
                    }else if (TextUtils.equals(result.getDevice().getName(),"Proove Urban")){
                        bleAdvMsg.setPid(0xDDDD);
                    }else {
                        bleAdvMsg.setPid(0xAAAA);
                    }
                }
                bleAdvMsg.setEdrMac(parseMacAddress(result.getScanRecord().getBytes()));
                notifyBleDeviceDiscovery(result.getDevice(), bleAdvMsg);
            } else if (bleData != null) {
                BleAdvMsg bleAdvMsg = new BleAdvMsg(bleData);
                notifyBleDeviceDiscovery(result.getDevice(), bleAdvMsg);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            LogUtil.error(TAG, "onScanFailed : " + errorCode);
        }
    };

    public static String parseMacAddress(byte[] rawBytes) {
        // 解析广播数据
        int index = 0;
        while (index < rawBytes.length) {
            int length = rawBytes[index] & 0xFF;
            if (length == 0) break;

            int type = rawBytes[index + 1] & 0xFF;
            // 找到0xFF类型的数据
            if (type == 0xFF) {
                // MAC地址在0xFFFF后的6个字节
                byte[] macBytes = new byte[6];
                System.arraycopy(rawBytes, index + 4, macBytes, 0, 6);

                // 将字节数组转换为十六进制字符串，反序处理
                StringBuilder macAddress = new StringBuilder();
                for (int i = macBytes.length - 1; i >= 0; i--) {  // 从最后一个字节开始遍历
                    if (i < macBytes.length - 1) {
                        macAddress.append(":");
                    }
                    macAddress.append(String.format("%02X", macBytes[i]));
                }

                return macAddress.toString();
            }
            index += length + 1;
        }
        return "null";
    }

    private BluetoothManager() {
    }

    private static final class SingleTon {
        public static final BluetoothManager instance = new BluetoothManager();
    }

    public static BluetoothManager getInstance() {
        return SingleTon.instance;
    }

    public void init(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(bluetoothReceiver, filter);
        getA2dpProfileProxy(context, a2dpServiceListener);
        getHfpProfileProxy(context, hfpServiceListener);
    }

    @SuppressLint("MissingPermission")
    public void scanLeDevice() {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (bluetoothAdapter == null) {
            return;
        }
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        if (bluetoothLeScanner == null) {
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            return;
        }
        LogUtil.info(TAG, "scanLeDevice...");
        handler.removeCallbacks(stopScanTask);
        //handler.postDelayed(stopScanTask, SCAN_PERIOD);
        scanning = true;
        bluetoothLeScanner.startScan(leScanCallback);
    }

    public boolean isScanning() {
        return scanning;
    }

    @SuppressLint("MissingPermission")
    public void stopLeScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return;
        }
        if (bluetoothLeScanner != null) {
            LogUtil.info(TAG, "stopLeScan");
            scanning = false;
            handler.removeCallbacks(stopScanTask);
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    public boolean getA2dpProfileProxy(Context context, BluetoothProfile.ServiceListener listener) {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        return defaultAdapter.getProfileProxy(context, listener, BluetoothProfile.A2DP);
    }

    public boolean getHfpProfileProxy(Context context, BluetoothProfile.ServiceListener listener) {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        return defaultAdapter.getProfileProxy(context, listener, BluetoothProfile.HEADSET);
    }

    public boolean connectHfp(BluetoothHeadset hfp, BluetoothDevice device) {
        boolean ret = false;
        Method connect = null;
        try {
            connect = BluetoothHeadset.class.getDeclaredMethod("connect", BluetoothDevice.class);
            connect.setAccessible(true);
            ret = (boolean) connect.invoke(hfp, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean connectA2dp(BluetoothA2dp a2dp, BluetoothDevice device) {
        boolean ret = false;
        Method connect = null;
        try {
            connect = BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
            connect.setAccessible(true);
            ret = (boolean) connect.invoke(a2dp, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void connect2(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        connectA2dp(bluetoothA2dp, device);
        connectHfp(bluetoothHeadset, device);
    }

    public void connect(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        connectA2dp(bluetoothA2dp, device);
        connectHfp(bluetoothHeadset, device);
    }

    /**
     * 广播监听蓝牙状态
     */
    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            dealDeviceDiscovery(intent);
            dealConnectionReceive(intent);
        }
    }

    @SuppressLint("MissingPermission")
    private void dealDeviceDiscovery(Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            LogUtil.info(TAG, "DeviceDiscovery name = " + device.getName() + " mac = " + device.getAddress());
            notifyDeviceDiscovery(device);
        }
    }

    private void dealConnectionReceive(Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, DEFAULT_VALUE_BLUETOOTH);
            notifyBluetoothState(state);
            LogUtil.info(TAG, "蓝牙开关状态 ：" + state);
        } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, DEFAULT_VALUE_BLUETOOTH);
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            LogUtil.info(TAG, "设备连接状态 ：mac = " + bluetoothDevice.getAddress() + " state = " + state);
        } else if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, DEFAULT_VALUE_BLUETOOTH);
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            LogUtil.info(TAG, "A2dp连接状态 ：mac = " + bluetoothDevice.getAddress() + " state = " + state);
            notifyA2dpStateChange(bluetoothDevice, state);
        } else if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, DEFAULT_VALUE_BLUETOOTH);
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            LogUtil.info(TAG, "Hfp连接状态 ：mac = " + bluetoothDevice.getAddress() + " state = " + state);
        } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            LogUtil.info(TAG, "ACL已连接 ：mac = " + bluetoothDevice.getAddress());
            notifyConnectionChange(bluetoothDevice, BluetoothAdapter.STATE_CONNECTED);
        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            LogUtil.info(TAG, "ACL已断连 ：mac = " + bluetoothDevice.getAddress());
            notifyConnectionChange(bluetoothDevice, BluetoothAdapter.STATE_DISCONNECTED);
        }
    }

    public void connectRcsp(String mac) {
        RCSPController.getInstance().connectDevice(BluetoothUtil.getBluetoothDevice(mac));
    }

    public void disconnectRcsp(String mac) {
        RCSPController.getInstance().disconnectDevice(BluetoothUtil.getBluetoothDevice(mac));
    }

    @SuppressLint("MissingPermission")
    public void startDiscovery() {
        if (!bluetoothAdapter.isEnabled()) {
            return;
        }
        bluetoothAdapter.startDiscovery();
    }

    @SuppressLint("MissingPermission")
    public void cancelDiscovery() {
        if (!bluetoothAdapter.isEnabled()) {
            return;
        }
        bluetoothAdapter.cancelDiscovery();
    }

    public void addBluetoothEventListener(IBluetoothEventListener listener) {
        if (listener == null) {
            return;
        }
        bluetoothEventListeners.add(listener);
    }

    public void removeBluetoothEventListener(IBluetoothEventListener listener) {
        if (listener == null) {
            return;
        }
        bluetoothEventListeners.remove(listener);
    }

    @SuppressLint("MissingPermission")
    public Set<BluetoothDevice> getBondedDevice() {
        return bluetoothAdapter.getBondedDevices();
    }

    private void notifyDeviceDiscovery(BluetoothDevice device) {
        for (IBluetoothEventListener listener : bluetoothEventListeners) {
            listener.onDiscovery(device);
        }
    }

    private void notifyBleDeviceDiscovery(BluetoothDevice device, BleAdvMsg bleAdvMsg) {
        for (IBluetoothEventListener listener : bluetoothEventListeners) {
            listener.onBleDiscovery(device, bleAdvMsg);
        }
    }

    private void notifyConnectionChange(BluetoothDevice device, int state) {
        for (IBluetoothEventListener listener : bluetoothEventListeners) {
            listener.onConnectionChange(device, state);
        }
    }

    private void notifyA2dpStateChange(BluetoothDevice device, int state) {
        for (IBluetoothEventListener listener : bluetoothEventListeners) {
            listener.onA2dpStateChange(device, state);
        }
    }

    private void notifyBluetoothState(int state) {
        for (IBluetoothEventListener listener : bluetoothEventListeners) {
            listener.onBluetoothState(state);
        }
    }

    public abstract static class IBluetoothEventListener {
        public void onBluetoothState(int state) {

        }

        public void onDiscovery(BluetoothDevice device) {

        }

        public void onConnectionChange(BluetoothDevice device, int state) {

        }

        public void onA2dpStateChange(BluetoothDevice device, int state) {

        }

        public void onBleDiscovery(BluetoothDevice device, BleAdvMsg bleAdvMsg) {
            Log.i(TAG, "onBleDiscovery: "+new Gson().toJson(bleAdvMsg));
        }
    }
}
