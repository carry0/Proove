package com.proove.ble.constant.protocol;

import com.yscoco.lib.util.ByteUtil;

public class CommonMsg {
    private final byte headFlag;
    private final byte commandId;
    private final byte statusCode;
    private final int dataLength;
    private final byte[] data;
    private final byte[] checkSum;

    public CommonMsg(byte[] rawData) {
        headFlag = rawData[0];
        commandId = rawData[1];
        statusCode = rawData[2];
        dataLength = ByteUtil.bytesToInt(rawData[3], rawData[4]);
        if (dataLength > 0) {
            data = ByteUtil.sliceByteArray(rawData, 5, dataLength);
        } else {
            data = new byte[]{};
        }
        checkSum = ByteUtil.sliceByteArray(rawData, rawData.length - 2, 2);
    }

    public byte getHeadFlag() {
        return headFlag;
    }

    public byte getCommandId() {
        return commandId;
    }

    public byte getStatusCode() {
        return statusCode;
    }

    public int getDataLength() {
        return dataLength;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getCheckSum() {
        return checkSum;
    }
}
