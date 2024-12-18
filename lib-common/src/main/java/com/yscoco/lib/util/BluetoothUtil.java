package com.yscoco.lib.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class BluetoothUtil {
    private static final String TAG = "BluetoothUtil";

    /**
     * 检查一个字符串是否是蓝牙地址
     *
     * @param address 要检查的字符串
     * @return 如果字符串是蓝牙地址，则返回true；否则返回false
     */
    public static boolean isBluetoothAddress(String address) {
        // 检查字符串长度是否为17个字符
        if (address == null || address.length() != 17) {
            return false; // 如果长度不是17，那么这不是一个蓝牙地址，直接返回false
        }

        // 检查字符串中是否只包含十六进制字符和冒号
        for (int i = 0; i < address.length(); i++) {
            char c = address.charAt(i);
            if (i % 3 == 2) {
                if (c != ':') {
                    return false; // 如果不是冒号，则这不是一个蓝牙地址，直接返回false
                }
            } else {
                if ((c < '0' || c > '9') && (c < 'a' || c > 'f') && (c < 'A' || c > 'F')) {
                    return false; // 如果不是十六进制字符，则这不是一个蓝牙地址，直接返回false
                }
            }
        }
        return true; // 字符串符合蓝牙地址的格式，返回true
    }

    /**
     * 掩盖MAC地址中的部分信息。
     *
     * @param mac 要掩盖的MAC地址
     * @return 掩盖后的MAC地址
     */
    public static String maskMacAddress(String mac) {
        if (!isBluetoothAddress(mac)) {
            return mac; // 如果MAC地址为null，返回null
        }

        // 将MAC地址分成6个部分
        String[] parts = mac.split(":");

        // 掩盖第3和第4部分的信息
        parts[2] = "**";
        parts[3] = "**";

        // 拼接掩盖后的MAC地址
        StringBuilder maskedMacAddress = new StringBuilder();
        for (String part : parts) {
            if (maskedMacAddress.length() > 0) {
                maskedMacAddress.append(":");
            }
            maskedMacAddress.append(part);
        }
        return maskedMacAddress.toString(); // 返回掩盖后的MAC地址
    }


    /**
     * 判断给定的设备mac地址是否已连接经典蓝牙
     *
     * @param macAddress 设备mac地址,例如"78:02:B7:01:01:16"
     */
    public static boolean isConnectClassicBT(String macAddress) {
        if (!isBluetoothAddress(macAddress)) {
            return false;
        }
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;
        try {
            //是否存在连接的蓝牙设备
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(bluetoothAdapter, (Object[]) null);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                BluetoothDevice device = getBluetoothDevice(macAddress);
                if (device != null) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    return (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDeviceAlias(String mac) {
        String alias = "";
        BluetoothDevice bluetoothDevice = getBluetoothDevice(mac);
        if (bluetoothDevice == null) {
            return alias;
        }
        return bluetoothDevice.getAlias();
    }

    public static BluetoothDevice getBluetoothDevice(String mac) {
        if (!isBluetoothAddress(mac)) {
            return null;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return null;
        }
        return bluetoothAdapter.getRemoteDevice(mac);
    }

    @SuppressLint("MissingPermission")
    public static Set<BluetoothDevice> getBondedDeviceList() {
        Set<BluetoothDevice> deviceList = new HashSet<>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return deviceList;
        }
        return bluetoothAdapter.getBondedDevices();
    }

    public static boolean isBluetoothEnable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }


    public static boolean removeBond(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null) {
            return false;
        }
        try {
            Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
            Boolean returnValue = (Boolean) removeBondMethod.invoke(bluetoothDevice);
            return returnValue.booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
