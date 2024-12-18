package com.proove.smart.manager;


import static com.jieli.bluetooth.constant.StateCode.CONNECTION_CONNECTED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_FAILED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_OK;
import static com.proove.smart.manager.BluetoothManager.parseMacAddress;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import androidx.room.Room;

import com.jieli.bluetooth.bean.base.BaseError;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.rcsp.callback.BTRcspEventCallback;
import com.jieli.bluetooth.interfaces.rcsp.callback.OnRcspActionCallback;
import com.proove.ble.constant.Constant;
import com.proove.ble.constant.DeviceInfoType;
import com.proove.ble.constant.Product;
import com.proove.ble.data.BleAdvMsg;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.SimpleLocation;
import com.proove.ble.entity.DeviceInfoEntity;
import com.proove.smart.database.AppDatabase;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.ContextUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceManager extends BluetoothManager.IBluetoothEventListener {
    private static final String TAG = "DeviceManager";
    private String currentMac;
    private static final Map<String, DeviceInfo> deviceInfoMap = new HashMap<>();
    private static final Set<IDeviceEventListener> deviceEventListeners = new HashSet<>();
    private static final long GET_LOCATION_TIMEOUT = 5000;
    private final BTRcspEventCallback btRcspEventCallback = new BTRcspEventCallback() {
        @Override
        public void onConnection(BluetoothDevice device, int status) {
            super.onConnection(device, status);
            switch (status) {
                case CONNECTION_CONNECTED:
                case CONNECTION_OK:
                    DeviceInfo deviceInfo = deviceInfoMap.get(device.getAddress());
                    if (deviceInfo != null) {
                        Log.i(TAG, "CONNECTION_OK: deviceInfo");
                        stopAdvNotify(device.getAddress());
                    }
                    Log.i(TAG, "CONNECTION_OK: deviceInfo is null");
                    break;
                case CONNECTION_FAILED:
                    Log.i(TAG, "CONNECTION_FAILED: ");
                    break;
            }
        }
    };
    private Handler handler;
    private AppDatabase database;

    private DeviceManager() {
    }

    public static DeviceManager getInstance() {
        return Singleton.instance;
    }

    @Override
    public void onBluetoothState(int state) {
        super.onBluetoothState(state);
        if (state == BluetoothAdapter.STATE_OFF) {
            for (DeviceInfo deviceInfo : deviceInfoMap.values()) {
                deviceInfo.setConnected(false);
                notifyConnectionChange(deviceInfo, BluetoothAdapter.STATE_DISCONNECTED);
            }
        }
    }

    @Override
    public void onA2dpStateChange(BluetoothDevice device, int state) {
        DeviceInfo deviceInfo = deviceInfoMap.get(device.getAddress());
        if (deviceInfo == null) {
            return;
        }
        if (state == BluetoothAdapter.STATE_CONNECTED) {
            deviceInfo.setConnected(true);
            if (deviceInfo.getMac().equals(getCurrentMac())) {
                BluetoothManager.getInstance().connectRcsp(device.getAddress());
            }
            getLocation(device.getAddress());
        } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {
            deviceInfo.setConnected(false);
            getLocation(device.getAddress());
        }
        notifyConnectionChange(deviceInfo, state);
    }


    private static final class Singleton {
        private static final DeviceManager instance = new DeviceManager();
    }

    public void init() {
        BluetoothManager.getInstance().addBluetoothEventListener(this);
        RCSPController.getInstance().addBTRcspEventCallback(btRcspEventCallback);
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {
            database = Room
                    .databaseBuilder(ContextUtil.getAppContext(), AppDatabase.class, Constant.APP_DATABASE_NAME)
                    .build();
            getDatabaseDeviceInfoList();
        });
    }

    public String getCurrentMac() {
        return currentMac;
    }

    public void setCurrentMac(String currentMac) {
        if (BluetoothUtil.isBluetoothAddress(currentMac)) {
            this.currentMac = currentMac;
        }
    }

    public void addDevice(DeviceInfo deviceInfo) {
        if (deviceInfo == null || !BluetoothUtil.isBluetoothAddress(deviceInfo.getMac())) {
            return;
        }
        LogUtil.info(TAG, "addDevice = " + deviceInfo);
        deviceInfoMap.put(deviceInfo.getMac(), deviceInfo);
        notifyDeviceListChange(getDeviceInfoList());
        if (deviceInfo.isConnected()) {
            getLocation(deviceInfo.getMac());
        }
    }

    public void saveDevice(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return;
        }
        handler.post(() -> {
            database.deviceInfoDao().addDevice(new DeviceInfoEntity(deviceInfo));
        });
    }

    public void removeDevice(String mac) {
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return;
        }
        handler.post(() -> {
            DeviceInfoEntity deviceInfoEntity = new DeviceInfoEntity();
            deviceInfoEntity.setMac(mac);
            database.deviceInfoDao().removeDevice(deviceInfoEntity);
        });
        notifyDeviceListChange(getDeviceInfoList());
        if (deviceInfoMap.isEmpty()) {
            return;
        }
        deviceInfoMap.remove(mac);
    }

    public DeviceInfo getDeviceInfo(String mac) {
        return deviceInfoMap.get(mac);
    }

    public DeviceInfo getCurrentDeviceInfo() {
        return deviceInfoMap.get(currentMac);
    }

    public List<DeviceInfo> getDeviceInfoList() {
        return new ArrayList<>(deviceInfoMap.values());
    }

    private void getDatabaseDeviceInfoList() {
        handler.post(() -> {
            List<DeviceInfoEntity> deviceInfoEntities = database.deviceInfoDao().getDeviceInfoList();
            for (DeviceInfoEntity deviceInfoEntity : deviceInfoEntities) {
                DeviceInfo deviceInfo = new DeviceInfo(deviceInfoEntity);
                Product product = Product.getProductById(deviceInfo.getProductId());
                if (product == null) {
                    continue;
                }
                deviceInfo.setProductName(product.getProductName());
                deviceInfo.setImageResId(product.getProductImage());
                deviceInfo.setIconResId(product.getProductIcon());
                deviceInfo.setDeviceName(deviceInfo.getDeviceName());
                deviceInfo.setConnected(BluetoothUtil.isConnectClassicBT(deviceInfo.getMac()));
                addDevice(deviceInfo);
            }
        });
    }

    public void getLocation(String mac) {
        DeviceInfo deviceInfo = getDeviceInfo(mac);
        if (deviceInfo == null) {
            return;
        }
        if (deviceInfo.equalsDevice()) {
            LocationManager.getInstance().addLocationListener(new LocationManager.ILocationListener() {
                @Override
                public void onTrackUpdate(List<SimpleLocation> track) {
                    // 处理轨迹更新
                    for (SimpleLocation point : track) {
                        Log.d("Track", "位置: " + point.getAddressStr() +
                                " 经度: " + point.getLongitude() +
                                " 纬度: " + point.getLatitude());
                    }
                }

                @Override
                public void onLocationError(String errorMessage) {
                    // 处理错误
                    Log.i(TAG, "onLocationError: " + errorMessage);
                }
            });
        } else if (deviceInfo.equalsEarDevice() || deviceInfo.isConnected()) {
            LocationManager.getInstance().getLastLocation(new LocationManager.ILocationListener() {
                @Override
                public void onLastLocation(SimpleLocation simpleLocation) {
                    if (simpleLocation == null) {
                        return;
                    }
                    saveLocationToDevice(mac, simpleLocation);
                }

                @Override
                public void onLocationError(String errorMessage) {
                    super.onLocationError(errorMessage);
                    Log.i(TAG, "onLocationError: 定位失败");
                }
            });
        }
    }

    private void saveLocationToDevice(String mac, SimpleLocation simpleLocation) {
        DeviceInfo device = getDeviceInfo(mac);
        if (device != null && simpleLocation != null) {
            device.setLocation(simpleLocation.getAddressStr());
            device.setLongitude(String.valueOf(simpleLocation.getLongitude()));
            device.setLatitude(String.valueOf(simpleLocation.getLatitude()));
            device.setLastRecordTime(System.currentTimeMillis());
            DeviceManager.getInstance().saveDevice(device);
        }
    }


    public Product getCurrentProduct() {
        return getProduct(getCurrentMac());
    }

    public Product getProduct(String mac) {
        DeviceInfo deviceInfo = getDeviceInfo(mac);
        if (deviceInfo == null) {
            return null;
        }
        Product product = Product.getProductById(deviceInfo.getProductId());
        if (product == null) {
            return null;
        }
        return product;
    }

    public void stopAdvNotify(String mac) {
        BluetoothDevice device = BluetoothUtil.getBluetoothDevice(mac);
        RCSPController controller = RCSPController.getInstance();
        controller.controlAdvBroadcast(device, false, new OnRcspActionCallback<>() {
            @Override
            public void onSuccess(BluetoothDevice device, Boolean message) {
                //成功回调
                //enable = true, 开启设备广播信息，数据将在BTRcspEventCallback#onDeviceBroadcast回调
                //enable = false, 关闭设备广播信息
                LogUtil.info(TAG, "stopAdvNotify onSuccess");
            }

            @Override
            public void onError(BluetoothDevice device, BaseError error) {
                //失败回调
                //error - 错误信息
                LogUtil.info(TAG, "stopAdvNotify onError");
            }
        });
    }

    public boolean saveDeviceName(String mac, String name) {
        DeviceInfo deviceInfo = getDeviceInfo(mac);
        if (deviceInfo == null) {
            return false;
        }
        if (StringUtil.isNullOrEmpty(name) || name.length() > Constant.DEVICE_NAME_LIMIT) {
            return false;
        }
        deviceInfo.setDeviceName(name);
        saveDevice(deviceInfo);
        notifyDeviceInfoChange(DeviceInfoType.DEVICE_NAME, deviceInfo);
        return true;
    }

    public boolean isDeviceConnected(String mac) {
        DeviceInfo deviceInfo = getDeviceInfo(mac);
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.isConnected();
    }

    public void addDeviceEventListener(IDeviceEventListener listener) {
        if (listener == null) {
            return;
        }
        deviceEventListeners.add(listener);
    }

    public void removeDeviceEventListener(IDeviceEventListener listener) {
        if (listener == null) {
            return;
        }
        deviceEventListeners.remove(listener);
    }

    private void notifyConnectionChange(DeviceInfo device, int state) {
        for (IDeviceEventListener listener : deviceEventListeners) {
            listener.onConnectionChange(device, state);
        }
    }

    private void notifyDeviceListChange(List<DeviceInfo> deviceInfoList) {
        LogUtil.info(TAG, "notifyDeviceListChange = " + deviceInfoList.size());
        for (IDeviceEventListener listener : deviceEventListeners) {
            listener.onDeviceListChange(deviceInfoList);
        }
    }

    private void notifyDeviceInfoChange(DeviceInfoType type, DeviceInfo deviceInfo) {
        for (IDeviceEventListener listener : deviceEventListeners) {
            listener.onDeviceInfoChange(type, deviceInfo);
        }
    }

    public interface IDeviceEventListener {
        void onDeviceInfoChange(DeviceInfoType type, DeviceInfo deviceInfo);

        void onConnectionChange(DeviceInfo device, int state);

        void onDeviceListChange(List<DeviceInfo> deviceInfoList);
    }

    public void onClean() {
        RCSPController controller = RCSPController.getInstance();
        controller.disconnectDevice(controller.getUsingDevice());
        controller.registerOnRcspEventListener(btRcspEventCallback);
    }
}
