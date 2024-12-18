package com.proove.smart.manager;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.proove.ble.constant.BleRequestType;
import com.proove.ble.constant.BleScanState;
import com.proove.ble.constant.BleServiceType;
import com.proove.ble.constant.BluetoothConstant;
import com.proove.ble.constant.Constant;
import com.proove.ble.constant.SpConstant;
import com.proove.ble.data.BleAdvMsg;
import com.proove.ble.data.BleRequest;
import com.proove.ble.data.DeviceInfo;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.ByteUtil;
import com.yscoco.lib.util.ContextUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.SpUtil;
import com.yscoco.lib.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressLint("MissingPermission")
public class BleManager {

    public static final String TAG = "BleManager";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private final Handler handler = new Handler();
    private static final long SCAN_PERIOD = 60000;
    private static final long REQUEST_TIMEOUT = 2000;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic commonWriteCharacteristic;
    private final HashMap<String, Integer> bleDeviceConnectionState = new HashMap<>();
    private final Set<IBleEventListener> bleEventListeners = new HashSet<>();
    private final Queue<BleRequest> requestQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isDeviceBusy;
    private boolean isServiceReady;
    private boolean isDiscoveryService;

    private final Runnable stopScanTask = this::stopLeScan;

    private final Runnable resetDiscoveryFlagTask = new Runnable() {
        @Override
        public void run() {
            isDiscoveryService = false;
        }
    };

    private final Runnable requestTimeoutTask = () -> {
        LogUtil.info(TAG, "request timeout");
        notifyRequest();
    };

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord == null) {
                return;
            }
            byte[] bleData = scanRecord.getManufacturerSpecificData(Constant.COMPANY_ID);
            if (bleData == null) {
                return;
            }
            BleAdvMsg bleAdvMsg = new BleAdvMsg(bleData);
            LogUtil.info(TAG, "scan result " + bleAdvMsg);
            notifyBleDeviceDiscovery(result.getDevice(), bleAdvMsg);
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

    @SuppressLint("MissingPermission")
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            BluetoothDevice device = gatt.getDevice();
            if (device == null) {
                return;
            }
            LogUtil.info(TAG, "onConnectionStateChange = "
                    + device.getAddress() + " " + device.getName() + " " + status + " " + newState);
            bleDeviceConnectionState.put(device.getAddress(), newState);


            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.requestMtu(Constant.BLE_MTU);
                isDiscoveryService = true;
                handler.postDelayed(resetDiscoveryFlagTask, 10000);
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                requestQueue.clear();
                isServiceReady = false;
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                requestQueue.clear();
            }
            notifyBleConnectionChange(device, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            LogUtil.info(TAG, "onServicesDiscovered status = " + status);
            isDiscoveryService = false;
            handler.removeCallbacks(resetDiscoveryFlagTask);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                initService();
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            byte[] value = characteristic.getValue();
            dealCharacteristicRead(gatt, characteristic, value, status);
        }

        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);
            //dealCharacteristicRead(gatt, characteristic, value, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            LogUtil.info(TAG, "onCharacteristicWrite uuid = " + characteristic.getUuid() + " status = " + status);
            notifyRequest();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            dealCharacteristicChange(gatt, characteristic, value);
        }

        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            super.onCharacteristicChanged(gatt, characteristic, value);
            //dealCharacteristicChange(gatt, characteristic, value);
        }

        @Override
        public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
            super.onDescriptorRead(gatt, descriptor, status, value);
            LogUtil.info(TAG, "onDescriptorRead value = " + Arrays.toString(value) + " status = " + status);
            notifyRequest();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            LogUtil.info(TAG, "onDescriptorWrite status = " + status);
            notifyRequest();
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            LogUtil.info(TAG, "MTU = " + mtu);
            gatt.discoverServices();
        }
    };

    private void dealCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, byte[] value, int status) {
        LogUtil.info(TAG, "onCharacteristicRead value = " + Arrays.toString(value) + " status = " + status);
        notifyRequest();
    }

    private void dealCharacteristicChange(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, byte[] value) {
        LogUtil.info(TAG, "onCharacteristicChanged value = " + ByteUtil.bytesToHexString(value));
        String uuid = characteristic.getUuid().toString();
        BleServiceType bleServiceType;
        if (TextUtils.equals(BluetoothConstant.COMMON_CHARACTERISTIC_NOTIFY_UUID,uuid)) {
            bleServiceType = BleServiceType.COMMON_SERVICE;
        } else {
            return;
        }
        notifyBleDataNotify(gatt.getDevice(), bleServiceType, value);
    }

    private void notifyRequest() {
        handler.removeCallbacks(requestTimeoutTask);
        isDeviceBusy = false;
        tryRequest();
    }

    @SuppressLint("MissingPermission")
    private void showServices(BluetoothGatt gatt) {
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService gattService : services) {
            LogUtil.info(TAG, "----Service UUID = " + gattService.getUuid());
            for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                LogUtil.info(TAG, "--------Characteristic UUID = " + characteristic.getUuid());
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    LogUtil.info(TAG, "------------Descriptor UUID = " + descriptor.getUuid());
                }
            }
        }
    }

    private BleManager() {
    }

    private static final class SingleTon {
        public static final BleManager instance = new BleManager();
    }

    public static BleManager getInstance() {
        return SingleTon.instance;
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
        handler.removeCallbacks(stopScanTask);
        if (!scanning) {
            scanning = true;
            LogUtil.info(TAG, "startScan");
            bluetoothLeScanner.startScan(leScanCallback);
            notifyBleScanState(BleScanState.BLE_SCAN_START);
        } else {
            stopLeScan();
        }
    }

    @SuppressLint("MissingPermission")
    public void stopLeScan() {
        if (!BluetoothUtil.isBluetoothEnable()) {
            return;
        }
        if (bluetoothLeScanner != null) {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            LogUtil.info(TAG, "stopLeScan");
            notifyBleScanState(BleScanState.BLE_SCAN_STOP);
        }
    }

    public String getCurrentMac() {
        if (bluetoothGatt == null) {
            return StringUtil.EMPTY;
        }
        return bluetoothGatt.getDevice().getAddress();
    }

    public synchronized void connectGatt(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        Integer state = bleDeviceConnectionState.get(device.getAddress());
        if (state != null && (state == BluetoothProfile.STATE_CONNECTING)) {
            LogUtil.info(TAG, "connectGatt fail connecting");
            return;
        }
        if (isDiscoveryService) {
            LogUtil.info(TAG, "connectGatt fail isDiscoveryService");
            return;
        }
        if (bluetoothGatt != null) {
            BluetoothDevice bluetoothDevice = bluetoothGatt.getDevice();
            if (bluetoothDevice != null && !device.getAddress().equals(bluetoothDevice.getAddress())) {
                disconnectGatt();
                closeGatt();
            }
        }
        LogUtil.info(TAG, "connectGatt device = " + device.getAddress());
        bleDeviceConnectionState.put(device.getAddress(), BluetoothProfile.STATE_CONNECTING);
        bluetoothGatt = device.connectGatt(ContextUtil.getAppContext(), false,
                bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    @SuppressLint("MissingPermission")
    public void disconnectGatt() {
        if (bluetoothGatt == null) {
            return;
        }
        BluetoothDevice device = bluetoothGatt.getDevice();
        bluetoothGatt.disconnect();
        isServiceReady = false;
        bleDeviceConnectionState.put(device.getAddress(), BluetoothProfile.STATE_DISCONNECTED);
        notifyBleConnectionChange(device, BluetoothProfile.STATE_DISCONNECTED);
    }

    public void closeGatt() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        isServiceReady = false;
        BluetoothDevice device = bluetoothGatt.getDevice();
        bleDeviceConnectionState.put(device.getAddress(), BluetoothProfile.STATE_DISCONNECTED);
        notifyBleConnectionChange(device, BluetoothProfile.STATE_DISCONNECTED);
    }

    @SuppressLint("MissingPermission")
    public void disconnectGatt(String mac) {
        if (bluetoothGatt == null || !BluetoothUtil.isBluetoothAddress(mac)) {
            return;
        }
        if (mac.equals(bluetoothGatt.getDevice().getAddress())) {
            disconnectGatt();
        }
    }

    public void closeGatt(String mac) {
        if (bluetoothGatt == null || !BluetoothUtil.isBluetoothAddress(mac)) {
            return;
        }
        if (mac.equals(bluetoothGatt.getDevice().getAddress())) {
            closeGatt();
        }
    }

    @SuppressLint("MissingPermission")
    public boolean isBleConnected(String mac) {
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return false;
        }
        Integer state = bleDeviceConnectionState.get(mac);
        if (state == null) {
            state = BluetoothProfile.STATE_DISCONNECTED;
        }
        return state == BluetoothProfile.STATE_CONNECTED;
    }

    public boolean isServiceReady(String mac) {
        if (bluetoothGatt == null || !BluetoothUtil.isBluetoothAddress(mac)) {
            return false;
        }
        if (!mac.equals(bluetoothGatt.getDevice().getAddress())) {
            return false;
        }
        return isServiceReady;
    }

    @SuppressLint("MissingPermission")
    private void initService() {
        if (bluetoothGatt == null) {
            return;
        }
        BluetoothDevice device = bluetoothGatt.getDevice();
        boolean isSuccess = initCommonService();
        if (isSuccess) {
            saveLastDeviceMac(device.getAddress());
            LogUtil.info(TAG, "initService success");
            notifyBleServiceReady(device);
            isServiceReady = true;
        } else {
            notifyBleServiceNotFound(device);
            isServiceReady = false;
        }
    }

    @SuppressLint("MissingPermission")
    private boolean initCommonService() {
        BluetoothGattService commonService = bluetoothGatt
                .getService(UUID.fromString(BluetoothConstant.COMMON_SERVICE_UUID));
        if (commonService == null) {
            LogUtil.error(TAG, "not found common service");
            return false;
        }
        this.commonWriteCharacteristic = commonService
                .getCharacteristic(UUID.fromString(BluetoothConstant.COMMON_CHARACTERISTIC_WRITE_UUID));
        BluetoothGattCharacteristic commonNotifyCharacteristic = commonService
                .getCharacteristic(UUID.fromString(BluetoothConstant.COMMON_CHARACTERISTIC_NOTIFY_UUID));
        BluetoothGattDescriptor commonNotifyDescriptor = commonNotifyCharacteristic
                .getDescriptor(UUID.fromString(BluetoothConstant.COMMON_DESCRIPTOR_NOTIFY_UUID));
        boolean isCommonNotify = bluetoothGatt.setCharacteristicNotification(commonNotifyCharacteristic, true);
        LogUtil.info(TAG, "commonNotifyCharacteristic isNotify = " + isCommonNotify);
        BleRequest bleRequest = new BleRequest(BleRequestType.DESCRIPTOR_WRITE,
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE,
                commonNotifyCharacteristic, commonNotifyDescriptor);
        request(bleRequest);
        return true;
    }

    @SuppressLint("MissingPermission")
    public void writeCommonData(byte[] data) {
        if (commonWriteCharacteristic == null || bluetoothGatt == null || data == null) {
            return;
        }
        LogUtil.info(TAG, "writeCommonData " + ByteUtil.bytesToHexString(data));
        BleRequest bleRequest = new BleRequest(BleRequestType.CHARACTERISTIC_WRITE,
                data, commonWriteCharacteristic, null);
        request(bleRequest);
    }

    private void request(BleRequest bleRequest) {
        if (bleRequest == null) {
            return;
        }
        requestQueue.offer(bleRequest);
        tryRequest();
    }

    private synchronized void tryRequest() {
        if (isDeviceBusy) {
            return;
        }
        if (bluetoothGatt == null) {
            return;
        }
        BleRequest headRequest = requestQueue.poll();
        if (headRequest == null) {
            LogUtil.info(TAG, "requestQueue is empty");
            return;
        }
        LogUtil.info(TAG, "tryRequest " + headRequest);
        boolean isSuccess = false;
        switch (headRequest.getBleRequestType()) {
            case CHARACTERISTIC_READ -> {
                isSuccess = bluetoothGatt
                        .readCharacteristic(headRequest.getTargetCharacteristic());
            }
            case CHARACTERISTIC_WRITE -> {
                headRequest.getTargetCharacteristic().setValue(headRequest.getData());
                isSuccess = bluetoothGatt.writeCharacteristic(headRequest.getTargetCharacteristic());
            }
            case DESCRIPTOR_READ -> {
                isSuccess = bluetoothGatt
                        .readDescriptor(headRequest.getTargetDescriptor());
            }
            case DESCRIPTOR_WRITE -> {
                headRequest.getTargetDescriptor().setValue(headRequest.getData());
                isSuccess = bluetoothGatt.writeDescriptor(headRequest.getTargetDescriptor());
            }
        }
        if (isSuccess) {
            isDeviceBusy = true;
            handler.postDelayed(requestTimeoutTask, REQUEST_TIMEOUT);
        }
    }

    private void notifyBleConnectionChange(BluetoothDevice device, int state) {
        for (IBleEventListener listener : bleEventListeners) {
            listener.onBleConnectionChange(device, state);
        }
    }

    private void notifyBleDeviceDiscovery(BluetoothDevice device, BleAdvMsg bleAdvMsg) {
        for (IBleEventListener listener : bleEventListeners) {
            listener.onBleDiscovery(device, bleAdvMsg);
        }
    }

    private void notifyBleDataNotify(BluetoothDevice device, BleServiceType bleServiceType, byte[] data) {
        for (IBleEventListener listener : bleEventListeners) {
            listener.onBleDataNotify(device, bleServiceType, data);
        }
    }

    private void notifyBleServiceReady(BluetoothDevice device) {
        for (IBleEventListener listener : bleEventListeners) {
            listener.onBleServiceReady(device);
        }
    }

    private void notifyBleServiceNotFound(BluetoothDevice device) {
        for (IBleEventListener listener : bleEventListeners) {
            listener.onBleServiceNotFound(device);
        }
    }

    private void notifyBleScanState(BleScanState state) {
        for (IBleEventListener listener : bleEventListeners) {
            listener.onBleScanState(state);
        }
    }

    public void addBleEventListener(IBleEventListener listener) {
        if (listener == null) {
            return;
        }
        bleEventListeners.add(listener);
    }

    public void removeBleEventListener(IBleEventListener listener) {
        if (listener == null) {
            return;
        }
        bleEventListeners.remove(listener);
    }

    private void saveLastDeviceMac(String mac) {
        SpUtil.getInstance().putString(SpConstant.LAST_BLE_DEVICE_MAC, mac);
    }

    private String getLastDeviceMac() {
        return SpUtil.getInstance().getString(SpConstant.LAST_BLE_DEVICE_MAC, StringUtil.EMPTY);
    }

    public abstract static class IBleEventListener {
        public void onBleScanState(BleScanState state) {

        }

        public void onBleDiscovery(BluetoothDevice device, BleAdvMsg bleAdvMsg) {

        }

        public void onBleConnectionChange(BluetoothDevice device, int state) {

        }

        public void onBleDataNotify(BluetoothDevice device, BleServiceType bleServiceType, byte[] data) {

        }

        public void onBleServiceReady(BluetoothDevice device) {

        }

        public void onBleServiceNotFound(BluetoothDevice device) {

        }
    }
}
