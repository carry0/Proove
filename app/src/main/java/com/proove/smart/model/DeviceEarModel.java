package com.proove.smart.model;


import com.proove.ble.constant.protocol.AncMode;
import com.proove.ble.constant.protocol.HearingMode;
import com.proove.ble.constant.protocol.LightsMode;
import com.proove.ble.constant.protocol.WorkMode;
import com.proove.ble.data.BatteryInfo;

public interface DeviceEarModel {
    interface IDataCallback<T> {
        void onResult(T data);
    }
    void onClean();

    void setConnectMac(String mac);

    void setConnectStatusListener(IDataCallback<Boolean> callback);

    void getDeviceName();

    void setDeviceName(String name, IDataCallback<Boolean> callback);

    void setDeviceNameListener(IDataCallback<String> callback);

    void getBattery();

    void setBatteryListener(IDataCallback<BatteryInfo> callback);

    void getAncMode();

    void setAncMode(AncMode mode);
    void get3DMode();
    void set3DMode(boolean is3D);
    void setIs3DCallback(IDataCallback<Boolean> callback);
    void setAncModeListener(IDataCallback<AncMode> callback);

    void getWorkMode();
    void getDeviceResetMode();
    void getLightsMode();
    void getTimingMode();
    void getHearingMode();
    void setHearingMode(HearingMode mode);
    void setHearingModeListener(IDataCallback<HearingMode> callback);
    void setLightsMode(LightsMode lightsMode);

    void setLightsModeListener(IDataCallback<LightsMode> callback);
    void setWorkMode(WorkMode workMode);

    void setWorkModeListener(IDataCallback<WorkMode> callback);

    void setTimingModeListener(IDataCallback<byte []> callback);

    void setTimingMode(byte [] timingMode);

    void getVolume();

    void setVolume(int volume);

    void setVolumeListener(IDataCallback<Integer> callback);

    void playOrPause();

    void previousSong();

    void nextSong();

    void getPlayState();

    void setPlayStateListener(IDataCallback<Boolean> callback);

    boolean isDeviceCall();

    boolean isMusicActive();

    void deleteDevice(String mac);

    void setRebootDeviceListener(IDataCallback<Boolean> callback);
    void rebootDevice();

    void resetEqData();
}
