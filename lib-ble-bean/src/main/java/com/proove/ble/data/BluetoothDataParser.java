package com.proove.ble.data;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDataParser {
    private static final byte HEADER_HIGH = (byte) 0xF5;
    private static final byte HEADER_LOW = (byte) 0x5F;

    // 命令字常量定义
    public static final int CMD_UPLOAD_ALL = 0x00;     // 上报全部数据
    public static final int CMD_COMBINATION = 0x01;    // 组合命令
    public static final int CMD_SPEED = 0x02;          // 速度
    public static final int CMD_BATTERY_LEVEL = 0x03;  // 电池电量格数
    public static final int CMD_BATTERY_PERCENT = 0x04;// 电池电量百分比
    public static final int CMD_CURRENT_MILEAGE = 0x05;// 本次里程
    public static final int CMD_CURRENT_TIME = 0x06;   // 本次骑行时间
    public static final int CMD_POWER_SWITCH = 0x08;   // 前灯开关
    public static final int CMD_SPEED_LIMIT = 0x0A;    // 速度限制
    public static final int CMD_UNIT = 0x0B;          // 单位设置
    public static final int CMD_TOTAL_MILEAGE = 0x0C; // 总里程
    public static final int CMD_CRUISE_MODE = 0x0d;    // 巡航开关
    public static final int CMD_DRIVE_MODE = 0x0e;     // 档位(行驶模式)
    public static final int CMD_BRAKE_MODE = 0x0f;     // 刹车模式
    public static final int CMD_ZERO_START = 0x10;     // 非零启动(启动模式)
    public static final int CMD_CONTROLLER_TEMP = 0x12; // 控制器温度
    public static final int CMD_ERROR_CODE = 0x19;     // 车辆状态
    public static final int CMD_FIRMWARE_VERSION = 0x65;// 电动车固件版本
    public static final int CMD_LOCK_STATUS = 0x66;    // 蓝牙锁开关(带密码)
    public static final int CMD_BATTERY_VOLTAGE = 0x67; // 电池电压


    private DeviceStatus cacheStatus = new DeviceStatus();
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 500; // 更新间隔，单位：毫秒

    /**
     * 状态回调接口
     */
    public interface DeviceStatusCallback {
        void onStatusChanged(DeviceStatus status);
    }

    private DeviceStatusCallback callback;

    /**
     * 设置状态回调
     */
    public void setCallback(DeviceStatusCallback callback) {
        this.callback = callback;
    }

    /**
     * 处理单条蓝牙数据
     */
    public void onDataReceived(byte[] data) {
        BluetoothData parsedData = parseData(data);
        if (parsedData == null) return;

        // 更新缓存状态
        updateCacheStatus(parsedData);

        // 检查是否需要触发回调
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
            if (callback != null) {
                callback.onStatusChanged(cacheStatus);
            }
            lastUpdateTime = currentTime;
        }
    }

    public static class BluetoothData {
        public int command;      // 命令字
        public int dataLength;   // 数据长度
        public byte[] data;      // 数据内容
        public int checksum;     // 校验和

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("命令字: 0x").append(String.format("%02X", command));
            result.append(", 数据长度: ").append(dataLength);
            result.append(", 数据内容: ");
            for (byte b : data) {
                result.append(String.format("%02X ", b));
            }
            result.append(", 校验和: 0x").append(String.format("%02X", checksum));
            return result.toString();
        }
    }

    public static class DeviceStatus {
        public double speed;           // 速度 km/h
        public int batteryLevel;       // 电池格数 0-5
        public int batteryPercentage;  // 电池百分比 0-100%
        public double currentMileage;  // 本次里程 km
        public int rideTime;           // 骑行时间 秒
        public boolean lightStatus;    // 前灯状态
        public int speedLimit;         // 限速值 km/h
        public boolean isMileUnit;     // 单位选择 true:英里 false:公里
        public double totalMileage;    // 总里程 km
        public boolean cruiseEnabled;      // 巡航状态
        public int driveMode;             // 档位模式 1:新手 2:常规 3:运动
        public int brakeMode;             // 刹车模式 1,2,3
        public boolean zeroStartEnabled;   // 非零启动状态
        public int controllerTemp;        // 控制器温度
        public int errorCode;             // 故障代码
        public String firmwareVersion;    // 固件版本
        public boolean isLocked;          // 锁定状态
        public double batteryVoltage;     // 电池电压

        @Override
        public String toString() {
            return String.format(
                    "设备状态:\n" +
                            "速度: %.1f km/h\n" +
                            "电池格数: %d格\n" +
                            "电池电量: %d%%\n" +
                            "本次里程: %.1f km\n" +
                            "骑行时间: %d秒\n" +
                            "前灯状态: %s\n" +
                            "限速值: %d km/h\n" +
                            "距离单位: %s\n" +
                            "总里程: %.1f km\n" +
                            "巡航状态: %s\n" +
                            "档位模式: %d\n" +
                            "刹车模式: %d\n" +
                            "非零启动: %s\n" +
                            "控制器温度: %d℃\n" +
                            "故障代码: %d\n" +
                            "固件版本: %s\n" +
                            "锁定状态: %s\n" +
                            "电池电压: %.1fV",
                    speed,
                    batteryLevel,
                    batteryPercentage,
                    currentMileage,
                    rideTime,
                    lightStatus ? "开启" : "关闭",
                    speedLimit,
                    isMileUnit ? "英里" : "公里",
                    totalMileage,
                    cruiseEnabled ? "开启" : "关闭",
                    driveMode,
                    brakeMode,
                    zeroStartEnabled ? "开启" : "关闭",
                    controllerTemp,
                    errorCode,
                    firmwareVersion,
                    isLocked ? "锁定" : "解锁",
                    batteryVoltage
            );
        }
    }
    /**
     * 更新缓存状态
     */
    private void updateCacheStatus(BluetoothData parsedData) {
        switch (parsedData.command) {
            case CMD_SPEED:
                cacheStatus.speed = getSpeed(parsedData.data);
                break;
            case CMD_BATTERY_LEVEL:
                cacheStatus.batteryLevel = getBatteryLevel(parsedData.data);
                break;
            case CMD_BATTERY_PERCENT:
                cacheStatus.batteryPercentage = getBatteryPercentage(parsedData.data);
                break;
            case CMD_CURRENT_MILEAGE:
                cacheStatus.currentMileage = getCurrentMileage(parsedData.data);
                break;
            case CMD_CURRENT_TIME:
                cacheStatus.rideTime = getRideTime(parsedData.data);
                break;
            case CMD_POWER_SWITCH:
                cacheStatus.lightStatus = getLightStatus(parsedData.data);
                break;
            case CMD_SPEED_LIMIT:
                cacheStatus.speedLimit = getSpeedLimit(parsedData.data);
                break;
            case CMD_UNIT:
                cacheStatus.isMileUnit = isUnitMile(parsedData.data);
                break;
            case CMD_TOTAL_MILEAGE:
                cacheStatus.totalMileage = getTotalMileage(parsedData.data);
                break;
            case CMD_CRUISE_MODE:
                cacheStatus.cruiseEnabled = getCruiseMode(parsedData.data);
                break;
            case CMD_DRIVE_MODE:
                cacheStatus.driveMode = getDriveMode(parsedData.data);
                break;
            case CMD_BRAKE_MODE:
                cacheStatus.brakeMode = getBrakeMode(parsedData.data);
                break;
            case CMD_ZERO_START:
                cacheStatus.zeroStartEnabled = getZeroStart(parsedData.data);
                break;
            case CMD_CONTROLLER_TEMP:
                cacheStatus.controllerTemp = getControllerTemp(parsedData.data);
                break;
            case CMD_ERROR_CODE:
                cacheStatus.errorCode = getErrorCode(parsedData.data);
                break;
            case CMD_FIRMWARE_VERSION:
                cacheStatus.firmwareVersion = getFirmwareVersion(parsedData.data);
                break;
            case CMD_LOCK_STATUS:
                cacheStatus.isLocked = getLockStatus(parsedData.data);
                break;
            case CMD_BATTERY_VOLTAGE:
                cacheStatus.batteryVoltage = getBatteryVoltage(parsedData.data);
                break;
        }
    }
    /**
     * 解析蓝牙数据
     */
    public static BluetoothData parseData(byte[] value) {
        if (value == null || value.length < 4) {
            return null;
        }

        // 检查帧头
        if (value[0]  != HEADER_HIGH || value[1] != HEADER_LOW) {
            return null;
        }

        BluetoothData result = new BluetoothData();
        result.command = value[2] & 0xFF;
        result.dataLength = value[3] & 0xFF;

        // 提取数据部分
        result.data = new byte[result.dataLength];
        System.arraycopy(value, 4, result.data, 0, result.dataLength);

        // 获取校验和
        result.checksum = value[value.length - 1] & 0xFF;

        return result;
    }

    /**
     * 获取速度值 (0x02)
     * @return 速度值，单位：10km/h
     */
    public static double getSpeed(byte[] data) {
        if (data.length >= 2) {
            int rawSpeed = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
            return rawSpeed / 10.0; // 转换为实际速度
        }
        return 0;
    }

    /**
     * 获取电池电量格数 (0x03)
     * @return 0-5格
     */
    public static int getBatteryLevel(byte[] data) {
        if (data.length >= 1) {
            return data[0] & 0xFF;
        }
        return 0;
    }

    /**
     * 获取电池电量百分比 (0x04)
     * @return 0-100%
     */
    public static int getBatteryPercentage(byte[] data) {
        if (data.length >= 1) {
            return data[0] & 0xFF;
        }
        return 0;
    }

    /**
     * 获取本次里程 (0x05)
     * @return 里程，单位：10km
     */
    public static double getCurrentMileage(byte[] data) {
        if (data.length >= 2) {
            int rawMileage = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
            return rawMileage / 10.0;
        }
        return 0;
    }

    /**
     * 获取本次骑行时间 (0x06)
     * @return 骑行时间，单位：秒
     */
    public static int getRideTime(byte[] data) {
        if (data.length >= 2) {
            return ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        }
        return 0;
    }

    /**
     * 获取前灯开关状态 (0x08)
     * @return true:开启 false:关闭
     */
    public static boolean getLightStatus(byte[] data) {
        if (data.length >= 1) {
            return (data[0] & 0xFF) == 1;
        }
        return false;
    }

    /**
     * 获取速度限制值 (0x0A)
     * @return 限速值，单位：km/h
     */
    public static int getSpeedLimit(byte[] data) {
        if (data.length >= 1) {
            return data[0] & 0xFF;
        }
        return 0;
    }

    /**
     * 获取单位设置 (0x0B)
     * @return true:英里 false:公里
     */
    public static boolean isUnitMile(byte[] data) {
        if (data.length >= 1) {
            return (data[0] & 0xFF) == 1;
        }
        return false;
    }

    /**
     * 获取总里程 (0x0C)
     * @return 总里程，单位：10km
     */
    public static double getTotalMileage(byte[] data) {
        if (data.length >= 4) {
            long rawMileage = ((long)(data[0] & 0xFF) << 24) |
                    ((long)(data[1] & 0xFF) << 16) |
                    ((long)(data[2] & 0xFF) << 8) |
                    (data[3] & 0xFF);
            return rawMileage / 10.0;
        }
        return 0;
    }

    /**
     * 获取巡航开关状态 (0x0D)
     * @return true:开启 false:关闭
     */
    public static boolean getCruiseMode(byte[] data) {
        if (data.length >= 1) {
            return (data[0] & 0xFF) == 1;
        }
        return false;
    }

    /**
     * 获取档位模式 (0x0E)
     * @return 1:新手 2:常规 3:运动
     */
    public static int getDriveMode(byte[] data) {
        if (data.length >= 1) {
            return data[0] & 0xFF;
        }
        return 1;
    }

    /**
     * 获取刹车模式 (0x0F)
     * @return 刹车值 1,2,3
     */
    public static int getBrakeMode(byte[] data) {
        if (data.length >= 1) {
            return data[0] & 0xFF;
        }
        return 1;
    }

    /**
     * 获取非零启动状态 (0x10)
     * @return true:非零启动 false:零启动
     */
    public static boolean getZeroStart(byte[] data) {
        if (data.length >= 1) {
            return (data[0] & 0xFF) == 1;
        }
        return false;
    }

    /**
     * 获取控制器温度 (0x12)
     * @return 温度值，单位：℃
     */
    public static int getControllerTemp(byte[] data) {
        if (data.length >= 2) {
            return ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        }
        return 0;
    }

    /**
     * 获取故障代码 (0x19)
     * @return 故障代码 0:normal
     */
    public static int getErrorCode(byte[] data) {
        if (data.length >= 1) {
            return data[0] & 0xFF;
        }
        return 0;
    }

    /**
     * 获取固件版本 (0x65)
     * @return 版本号字符串
     */
    public static String getFirmwareVersion(byte[] data) {
        if (data.length >= 12) {
            return new String(data, 0, 12);
        }
        return "";
    }

    /**
     * 获取锁定状态 (0x66)
     * @return true:锁定 false:解锁
     */
    public static boolean getLockStatus(byte[] data) {
        if (data.length >= 5) {
            return (data[0] & 0xFF) == 1;
        }
        return false;
    }

    /**
     * 获取电池电压 (0x67)
     * @return 电压值，单位：10V
     */
    public static double getBatteryVoltage(byte[] data) {
        if (data.length >= 2) {
            int rawVoltage = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
            return rawVoltage / 10.0;
        }
        return 0;
    }
}
