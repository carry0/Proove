package com.proove.ble.data;

public class BluetoothDataBuilder {
    private static final byte HEADER_HIGH = (byte) 0xF5;
    private static final byte HEADER_LOW = (byte) 0x5F;

    /**
     * 构建数据包
     * @param command 命令字
     * @param data 数据内容
     * @return 完整的数据包
     */
    public static byte[] buildPacket(int command, byte[] data) {
        int dataLength = data != null ? data.length : 0;
        byte[] packet = new byte[4 + dataLength + 1]; // 帧头(2) + 命令字(1) + 数据长度(1) + 数据内容(n) + 校验和(1)
        
        // 帧头
        packet[0] = HEADER_HIGH;
        packet[1] = HEADER_LOW;
        
        // 命令字
        packet[2] = (byte) command;
        
        // 数据长度
        packet[3] = (byte) dataLength;
        
        // 数据内容
        if (dataLength > 0) {
            System.arraycopy(data, 0, packet, 4, dataLength);
        }
        
        // 计算校验和
        packet[packet.length - 1] = calculateChecksum(packet);
        
        return packet;
    }

    /**
     * 计算校验和
     */
    private static byte calculateChecksum(byte[] packet) {
        int sum = 0;
        // 校验和计算不包括最后一个字节
        for (int i = 0; i < packet.length - 1; i++) {
            sum += packet[i] & 0xFF;
        }
        return (byte) sum;
    }

    /**
     * 设置非零启动模式
     * @param enabled true:非零启动 false:零启动
     */
    public static byte[] setZeroStart(boolean enabled) {
        return buildPacket(BluetoothDataParser.CMD_ZERO_START, new byte[]{(byte) (enabled ? 1 : 0)});
    }

    /**
     * 设置档位模式
     * @param mode 1:新手 2:常规 3:运动
     */
    public static byte[] setDriveMode(int mode) {
        if (mode < 1 || mode > 3) {
            mode = 1;
        }
        return buildPacket(BluetoothDataParser.CMD_DRIVE_MODE, new byte[]{(byte) mode});
    }

    /**
     * 设置刹车模式
     * @param mode 刹车模式 1,2,3
     */
    public static byte[] setBrakeMode(int mode) {
        if (mode < 1 || mode > 3) {
            mode = 1;
        }
        return buildPacket(BluetoothDataParser.CMD_BRAKE_MODE, new byte[]{(byte) mode});
    }

    /**
     * 设置巡航模式
     * @param enabled true:开启 false:关闭
     */
    public static byte[] setCruiseMode(boolean enabled) {
        return buildPacket(BluetoothDataParser.CMD_CRUISE_MODE, new byte[]{(byte) (enabled ? 1 : 0)});
    }

    /**
     * 设置前灯开关
     * @param enabled true:开启 false:关闭
     */
    public static byte[] setLightStatus(boolean enabled) {
        return buildPacket(BluetoothDataParser.CMD_POWER_SWITCH, new byte[]{(byte) (enabled ? 1 : 0)});
    }

    /**
     * 设置速度限制
     * @param limit 限速值(km/h)
     */
    public static byte[] setSpeedLimit(int limit) {
        return buildPacket(BluetoothDataParser.CMD_SPEED_LIMIT, new byte[]{(byte) limit});
    }

    /**
     * 设置单位
     * @param isMile true:英里 false:公里
     */
    public static byte[] setUnit(boolean isMile) {
        return buildPacket(BluetoothDataParser.CMD_UNIT, new byte[]{(byte) (isMile ? 1 : 0)});
    }
} 