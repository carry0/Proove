package com.proove.smart.model;

import static com.jieli.bluetooth.constant.StateCode.CONNECTION_CONNECTED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_CONNECTING;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_FAILED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_OK;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.rcsp.callback.BTRcspEventCallback;
import com.proove.ble.constant.BleServiceType;
import com.proove.ble.constant.Product;
import com.proove.ble.data.BleAdvMsg;
import com.proove.ble.data.DeviceInfo;
import com.proove.smart.manager.BleManager;
import com.proove.smart.manager.BluetoothManager;
import com.proove.smart.manager.DeviceManager;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeviceScanModelImpl extends BleManager.IBleEventListener implements DeviceScanModel {
    private static final String TAG = "DeviceScanModelImpl";

    private final BluetoothManager bluetoothManager = BluetoothManager.getInstance();
    private int pidFilter = Product.PID_ALL;
    private final Map<String, DeviceInfo> deviceInfoMap = new LinkedHashMap<>();
    private IDeviceListChangeCallback deviceListChangeCallback;
    private final HashMap<String, DeviceScanModel.IResultCallBack> connectCallBackMap = new HashMap<>();
    private final BTRcspEventCallback btRcspEventCallback = new BTRcspEventCallback() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            super.onConnection(device, status);
            LogUtil.info(TAG, "BTRcspEventCallback onConnection mac = "
                    + device.getAddress() + " status = " + status);
            DeviceScanModel.IResultCallBack connectCallBack = connectCallBackMap.get(device.getAddress());
            if (connectCallBack == null) {
                return;
            }
            switch (status) {
                case CONNECTION_CONNECTED:
                case CONNECTION_OK:
                    DeviceInfo deviceInfo = deviceInfoMap.get(device.getAddress());
                    if (deviceInfo != null) {
                        deviceInfo.setConnected(true);
                        deviceInfo.setLatestConnectTime(System.currentTimeMillis());
                        DeviceManager.getInstance().addDevice(deviceInfo);
                        DeviceManager.getInstance().saveDevice(deviceInfo);
                        DeviceManager.getInstance().stopAdvNotify(device.getAddress());
                    }
                    connectCallBack.onSuccess();
                    connectCallBackMap.remove(device.getAddress());
                    break;
                case CONNECTION_FAILED:
                    connectCallBack.onFail();
                    connectCallBackMap.remove(device.getAddress());
                    break;
            }
        }
    };

    @Override
    public void onBleConnectionChange(BluetoothDevice device, int state) {
        super.onBleConnectionChange(device, state);
        DeviceScanModel.IResultCallBack connectCallBack = connectCallBackMap.get(device.getAddress());
        if (connectCallBack == null) {
            return;
        }
        if (state == BluetoothProfile.STATE_CONNECTED) {
            DeviceInfo deviceInfo = deviceInfoMap.get(device.getAddress());
            if (deviceInfo != null) {
                deviceInfo.setConnected(true);
                deviceInfo.setLatestConnectTime(System.currentTimeMillis());
                DeviceManager.getInstance().addDevice(deviceInfo);
                DeviceManager.getInstance().saveDevice(deviceInfo);
            }
            connectCallBack.onSuccess();
            connectCallBackMap.remove(device.getAddress());
        } else {
            connectCallBack.onFail();
            connectCallBackMap.remove(device.getAddress());
        }
    }

    @Override
    public void connectBle(String mac, DeviceScanModel.IResultCallBack callBack) {
        connectCallBackMap.put(mac, callBack);
        DeviceManager.getInstance().setCurrentMac(mac);
        BluetoothDevice bluetoothDevice = BluetoothUtil.getBluetoothDevice(mac);
        BleManager.getInstance().connectGatt(bluetoothDevice);
    }

    private final BluetoothManager.IBluetoothEventListener bluetoothEventListener = new BluetoothManager.IBluetoothEventListener() {
        @Override
        public void onBleDiscovery(BluetoothDevice device, BleAdvMsg bleAdvMsg) {
            super.onBleDiscovery(device, bleAdvMsg);
            Log.i(TAG, "onBleDiscovery ：我进来了");
            dealDiscoveryDevice(device, bleAdvMsg);
        }
    };

    public DeviceScanModelImpl() {
        BluetoothManager.getInstance().addBluetoothEventListener(bluetoothEventListener);
        BleManager.getInstance().addBleEventListener(this);
    }

    @Override
    public void startBleScan(IDeviceListChangeCallback callback) {
        if (bluetoothManager.isScanning()) {
            bluetoothManager.stopLeScan();
            return;
        }
        deviceInfoMap.clear();
        this.deviceListChangeCallback = callback;
        bluetoothManager.scanLeDevice();
    }

    @Override
    public void stopBleScan() {
        bluetoothManager.stopLeScan();
    }

    @Override
    public void connectDevice(String mac, DeviceScanModel.IResultCallBack callBack) {
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            callBack.onFail();
            return;
        }
        DeviceInfo deviceInfo = deviceInfoMap.get(mac);
        if (deviceInfo == null) {
            connect(mac, callBack);
            return;
        }
        Product product = Product.getProductById(deviceInfo.getProductId());
        if (product == null) {
            return;
        }
        if (pidFilter != 0 && pidFilter != product.getProductId()) {
            return;
        }
        bluetoothManager.stopLeScan();
        connect(mac, callBack);

    }

    private void connect(String mac, IResultCallBack callBack) {
        RCSPController controller = RCSPController.getInstance();
        BluetoothDevice usingDevice = controller.getUsingDevice();
        if (usingDevice != null && !usingDevice.getAddress().equals(mac)) {
            controller.disconnectDevice(usingDevice);
        }
        connectCallBackMap.put(mac, callBack);
        DeviceManager.getInstance().setCurrentMac(mac);
        RCSPController.getInstance().addBTRcspEventCallback(btRcspEventCallback);
        RCSPController.getInstance().connectDevice(BluetoothUtil.getBluetoothDevice(mac));
    }


    @Override
    public void onClean() {
        stopBleScan();
        BluetoothManager.getInstance().removeBluetoothEventListener(bluetoothEventListener);
        RCSPController.getInstance().removeBTRcspEventCallback(btRcspEventCallback);
    }


    @SuppressLint("MissingPermission")
    private void dealDiscoveryDevice(BluetoothDevice device, BleAdvMsg bleAdvMsg) {
        if (device == null || bleAdvMsg == null) {
            return;
        }
        if (!Product.isSupportDevice(bleAdvMsg.getPid())) {
            return;
        }
        Product product = Product.getProductById(bleAdvMsg.getPid());
        if (product == null) {
            return;
        }
        LogUtil.info(TAG, "dealDiscoveryDevice = " + bleAdvMsg);
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
        deviceInfoMap.put(deviceInfo.getMac(), deviceInfo);

        notifyDeviceListChange();
    }

    private void notifyDeviceListChange() {
        if (deviceListChangeCallback == null) {
            Log.i(TAG, "deviceListChangeCallback is null: ");
            return;
        }
        List<DeviceInfo> deviceInfoList = new ArrayList<>();
        if (pidFilter == Product.PID_ALL) {
            deviceInfoList.addAll(deviceInfoMap.values());
        } else {
            for (DeviceInfo deviceInfo : deviceInfoMap.values()) {
                if (deviceInfo.getProductId() == pidFilter) {
                    deviceInfoList.add(deviceInfo);
                }
            }
        }
        deviceListChangeCallback.onDeviceListChange(deviceInfoList);
    }

    @Override
    public void release() {
        deviceListChangeCallback = null;
        deviceInfoMap.clear();
        RCSPController.getInstance().removeBTRcspEventCallback(btRcspEventCallback);
    }
}
