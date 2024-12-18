package com.proove.ble.data;

import com.yscoco.lib.util.ByteUtil;
import com.yscoco.lib.util.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BleAdvMsg {
    int bid;
    int pid;
    int edrConnectState;
    String edrMac;
    String license;

    public BleAdvMsg() {

    }

    public BleAdvMsg(byte[] data) {
        bid = ByteUtil.bytesToInt(data[0], data[1]);
        pid = ByteUtil.bytesToInt(data[2], data[3]);
        edrConnectState = data[4];
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 5; i < 11; i++) {
            if (i == 5) {
                stringBuilder.append(ByteUtil.byteToClearHexString((byte) (data[i] ^ 0xAD)));
            } else {
                stringBuilder.append(":").append(ByteUtil.byteToClearHexString((byte) (data[i] ^ 0xAD)));
            }
        }
        edrMac = stringBuilder.toString();
        license = new String(Arrays.copyOfRange(data, 11, 27), StandardCharsets.US_ASCII);
        if (!StringUtil.isMatch(license, StringUtil.REGEX_LICENSE)) {
            license = "0";
        }
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getEdrConnectState() {
        return edrConnectState;
    }

    public void setEdrConnectState(int edrConnectState) {
        this.edrConnectState = edrConnectState;
    }

    public String getEdrMac() {
        return edrMac;
    }

    public void setEdrMac(String edrMac) {
        this.edrMac = edrMac;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @Override
    public String toString() {
        return "BleAdvMsg{" +
                "bid=" + bid +
                ", pid=" + pid +
                ", edrConnectState=" + edrConnectState +
                ", edrMac='" + edrMac + '\'' +
                ", license='" + license + '\'' +
                '}';
    }
}
