package com.proove.smart.model;

import static com.jieli.bluetooth.constant.StateCode.CONNECTION_CONNECTED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_FAILED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_OK;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.rcsp.callback.BTRcspEventCallback;
import com.proove.ble.constant.Product;
import com.proove.ble.data.BleAdvMsg;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DevicePopInfo;
import com.proove.smart.manager.BluetoothManager;
import com.proove.smart.manager.DeviceManager;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.StringUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DevicePopModelImpl implements DevicePopModel {
    private static final String TAG = "DevicePopModelImpl";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final RCSPController controller = RCSPController.getInstance();
    private final Map<String, DeviceInfo> deviceInfoMap = new LinkedHashMap<>();
    private final HashMap<String, IResultCallBack> connectCallBackMap = new HashMap<>();
    private final HashMap<String, Long> blockTimeMap = new HashMap<>();
    private final BTRcspEventCallback btRcspEventCallback = new BTRcspEventCallback() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            super.onConnection(device, status);
            LogUtil.info(TAG, "BTRcspEventCallback onConnection mac = "
                    + device.getAddress() + " status = " + status);
            IResultCallBack connectCallBack = connectCallBackMap.get(device.getAddress());
            if (connectCallBack == null) {
                return;
            }
            switch (status) {
                case CONNECTION_CONNECTED:
                case CONNECTION_OK:
                    DeviceInfo deviceInfo = deviceInfoMap.get(device.getAddress());
                    if (deviceInfo != null) {
                        deviceInfo.setConnected(true);
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
    private IDataCallback<DevicePopInfo> devicePopInfoListener;
    private final BluetoothManager.IBluetoothEventListener bluetoothEventListener = new BluetoothManager.IBluetoothEventListener() {
        @Override
        public void onBleDiscovery(BluetoothDevice device, BleAdvMsg bleAdvMsg) {
            super.onBleDiscovery(device, bleAdvMsg);
            LogUtil.info(TAG, "bleAdvMsg " + bleAdvMsg);
            dealDiscoveryDevice(device, bleAdvMsg);
        }
    };

    private void notifyDevicePopInfo(DeviceInfo deviceInfo, Product product, boolean isShow) {
        DevicePopInfo devicePopInfo = new DevicePopInfo(product, deviceInfo, isShow);
        if (devicePopInfoListener != null) {
            devicePopInfoListener.onResult(devicePopInfo);
        }
    }

    public DevicePopModelImpl() {
        BluetoothManager.getInstance().addBluetoothEventListener(bluetoothEventListener);
        controller.addBTRcspEventCallback(btRcspEventCallback);
    }

    @Override
    public void onClean() {
        stopScan();
        BluetoothManager.getInstance().removeBluetoothEventListener(bluetoothEventListener);
        controller.removeBTRcspEventCallback(btRcspEventCallback);
    }


    @Override
    public void setDevicePopInfoListener(IDataCallback<DevicePopInfo> listener) {
        devicePopInfoListener = listener;
    }

    @Override
    public void startScan() {
        DeviceInfo currentDeviceInfo = DeviceManager.getInstance().getCurrentDeviceInfo();
        if (currentDeviceInfo != null && currentDeviceInfo.isConnected()) {
            stopScan();
            return;
        }
        BluetoothManager.getInstance().scanLeDevice();
    }

    @Override
    public void stopScan() {
//        controller.removeBTRcspEventCallback(btRcspEventCallback);
    }

    @Override
    public void addDeviceToBlock(String mac, long time) {
        LogUtil.info(TAG, "addDeviceToBlock " + mac + " " + time);
        blockTimeMap.put(mac, System.currentTimeMillis() + time);
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
        BluetoothDevice bluetoothDevice = BluetoothUtil.getBluetoothDevice(bleAdvMsg.getEdrMac());
        if (bluetoothDevice != null) {
            deviceName = bluetoothDevice.getName();
        }
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
        if (!isDeviceBlock(deviceInfo.getMac()) && BluetoothManager.getInstance().isScanning()
                && !DeviceManager.getInstance().isDeviceConnected(deviceInfo.getMac())) {
                    notifyDevicePopInfo(deviceInfo, product, true);
                }
    }

    @Override
    public void connectDevice(String mac, IResultCallBack callBack) {
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            callBack.onFail();
            return;
        }
        DeviceInfo deviceInfo = deviceInfoMap.get(mac);
        if (deviceInfo == null) {
            return;
        }
        Product product = Product.getProductById(deviceInfo.getProductId());
        if (product == null) {
            return;
        }
        stopScan();
        RCSPController controller = RCSPController.getInstance();
        BluetoothDevice usingDevice = controller.getUsingDevice();
        if (usingDevice != null && !usingDevice.getAddress().equals(mac)) {
            controller.disconnectDevice(usingDevice);
        }
        connectCallBackMap.put(mac, callBack);
        DeviceManager.getInstance().setCurrentMac(mac);
        handler.postDelayed(() ->  RCSPController.getInstance()
                .connectDevice(BluetoothUtil.getBluetoothDevice(mac)), 2000);

    }

    private boolean isDeviceBlock(String mac) {
        Long endTime = blockTimeMap.get(mac);
        if (endTime == null) {
            return false;
        }
        return System.currentTimeMillis() - endTime < 0;
    }
    public void release() {
        deviceInfoMap.clear();
        connectCallBackMap.clear();
        RCSPController.getInstance().removeBTRcspEventCallback(btRcspEventCallback);
    }
}
