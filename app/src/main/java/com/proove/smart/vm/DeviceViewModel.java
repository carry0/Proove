package com.proove.smart.vm;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jieli.bluetooth.bean.base.BaseError;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.rcsp.callback.OnRcspActionCallback;
import com.proove.ble.constant.Product;
import com.proove.ble.data.BleAdvMsg;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DeviceListItem;
import com.proove.smart.manager.BTRcspManager;
import com.proove.smart.manager.BluetoothManager;
import com.proove.smart.manager.BluetoothResult;
import com.proove.smart.manager.IBluetoothManager;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class DeviceViewModel extends AndroidViewModel implements IBluetoothManager.BluetoothCallback, BTRcspManager.BTRcspCallback{

    private static final String TAG = "DeviceViewModel";
    private final IBluetoothManager bluetoothManager;
    private final MutableLiveData<List<DeviceInfo>> discoveredDevices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);
    private final MutableLiveData<ConnectionState> connectionState = new MutableLiveData<>();
    private String connectMac = "";
    public enum ConnectionState {
        SUCCESS,
        FAIL,
        CONNECTING
    }
    public DeviceViewModel(@NonNull Application application) {
        super(application);
        bluetoothManager = IBluetoothManager.getInstance(application);
        bluetoothManager.registerCallback("callBack", this);
    }
    public LiveData<List<DeviceInfo>> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public LiveData<Boolean> getIsScanning() {
        return isScanning;
    }

    public LiveData<ConnectionState> getConnectionState() {
        return connectionState;
    }

    public void startScan() {
        if (!bluetoothManager.isScanning) {
            bluetoothManager.startScan();
            isScanning.setValue(true);
        }
    }

    public void stopScan() {
        if (bluetoothManager.isScanning) {
            bluetoothManager.stopScan();
            isScanning.setValue(false);
        }
    }

    public void connectDevice(DeviceInfo device) {
        stopScan();
        bluetoothManager.unregisterCallback("callBack");

        connectMac = device.getMac();
        connectionState.setValue(ConnectionState.CONNECTING);

        RCSPController controller = RCSPController.getInstance();
        BluetoothDevice usingDevice = controller.getUsingDevice();
        if (usingDevice != null && !usingDevice.getAddress().equals(device.getMac())) {
            controller.disconnectDevice(usingDevice);
        }
        controller.connectDevice(BluetoothUtil.getBluetoothDevice(device.getMac()));
        BTRcspManager.getInstance().registerCallback(this);
    }
    @Override
    public void onConnectSuccess(BluetoothDevice device) {
        if (!device.getAddress().equals(connectMac)) {
            return;
        }
//        if (bluetoothManager != null) {
//            bluetoothManager.release();
//        }
        connectionState.postValue(ConnectionState.SUCCESS);
        stopAdvNotify(connectMac);
    }

    @Override
    public void onConnectFail(BluetoothDevice device) {
        if (!device.getAddress().equals(connectMac)) {
            return;
        }
        if (bluetoothManager != null) {
            bluetoothManager.registerCallback("callBack", this);
        }
        connectionState.postValue(ConnectionState.FAIL);
    }

    @Override
    public void onBluetoothResult(BluetoothResult result) {
        if (result == null || !result.isSuccess()) {
            return;
        }
        switch (result.getSourceEvent().getType()) {
            case DEVICE_FOUND:
                if (result.getDevice() != null && result.getData() != null) {
                    handleDeviceFound(result.getDevice(), result.getData());
                }
                break;
            case DEVICE_DISCONNECTED:
                if (result.getDevice() != null) {
                    connectionState.postValue(ConnectionState.FAIL);
                }
                break;
        }
    }

    private void handleDeviceFound(BluetoothDevice device, byte[] bleData) {
        if (bleData == null) return;

        BleAdvMsg bleAdvMsg = new BleAdvMsg(bleData);
        if (!Product.isSupportDevice(bleAdvMsg.getPid())) return;

        Product product = Product.getProductById(bleAdvMsg.getPid());
        if (product == null) return;

        String deviceName = device.getName();
        if (StringUtil.isNullOrEmpty(deviceName)) {
            deviceName = product.getProductName();
        }

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setMac(bleAdvMsg.getEdrMac());
        deviceInfo.setBleMac(device.getAddress());
        deviceInfo.setDeviceName(deviceName);
        deviceInfo.setBrandId(bleAdvMsg.getBid());
        deviceInfo.setLicense(bleAdvMsg.getLicense());
        deviceInfo.setProductId(product.getProductId());
        deviceInfo.setIconResId(product.getProductIcon());
        deviceInfo.setImageResId(product.getProductImage());
        deviceInfo.setProductName(product.getProductName());
        deviceInfo.setConnected(bleAdvMsg.getEdrConnectState() == 1);

        updateDiscoveredDevice(deviceInfo);
    }

    private void updateDiscoveredDevice(DeviceInfo newDevice) {
        List<DeviceInfo> currentList = discoveredDevices.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }

        boolean deviceExists = false;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getBleMac().equals(newDevice.getBleMac())) {
                currentList.set(i, newDevice);
                deviceExists = true;
                break;
            }
        }

        if (!deviceExists) {
            currentList.add(newDevice);
        }

        discoveredDevices.postValue(currentList);
    }

    public void stopAdvNotify(String mac) {
        BluetoothDevice device = BluetoothUtil.getBluetoothDevice(mac);
        RCSPController.getInstance().controlAdvBroadcast(device, false, new OnRcspActionCallback<>() {
            @Override
            public void onSuccess(BluetoothDevice device, Boolean message) {
                LogUtil.info(TAG, "stopAdvNotify onSuccess");
            }

            @Override
            public void onError(BluetoothDevice device, BaseError error) {
                LogUtil.info(TAG, "stopAdvNotify onError");
            }
        });
    }
    public void cleanup() {
        if (bluetoothManager != null) {
            stopScan();
            bluetoothManager.release();
            bluetoothManager.unregisterCallback("callBack");
        }
        BTRcspManager.getInstance().unregisterCallback(this);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (bluetoothManager != null) {
            stopScan();
            bluetoothManager.release();
            bluetoothManager.unregisterCallback("callBack");
        }
        BTRcspManager.getInstance().unregisterCallback(this);
    }
}
