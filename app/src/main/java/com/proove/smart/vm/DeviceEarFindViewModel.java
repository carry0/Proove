package com.proove.smart.vm;

import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.proove.ble.constant.DeviceFindState;
import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.data.DeviceFindInfo;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DeviceMarker;
import com.proove.ble.data.SimpleLocation;
import com.proove.smart.manager.LocationManager;
import com.proove.smart.manager.MediaPlayManager;
import com.proove.smart.model.DeviceEarFindModel;
import com.proove.smart.model.DeviceEarFindModelImpl;
import com.proove.smart.manager.DeviceManager;
import com.yscoco.lib.util.DateUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.StringUtil;

public class DeviceEarFindViewModel extends ViewModel {
    private static final String TAG = "DeviceFindViewModel";
    private final DeviceEarFindModel model = new DeviceEarFindModelImpl();
    private MutableLiveData<DeviceFindInfo> deviceFindInfoLiveData;
    private MutableLiveData<Pair<Double, Double>> userLocationLiveData;
    private MutableLiveData<Float> directLiveData;
    private final MutableLiveData<DeviceInfo> currentDeviceLiveData = new MutableLiveData<>();
    private final LocationManager.ILocationListener locationListener = new LocationManager.ILocationListener() {
        @Override
        public void onUpdateLocation(SimpleLocation simpleLocation) {
            if (simpleLocation!=null&&userLocationLiveData != null) {
                userLocationLiveData.postValue(new Pair<>(simpleLocation.getLatitude(), simpleLocation.getLongitude()));
            }
        }

        @Override
        public void onDirectChange(float direct) {
            super.onDirectChange(direct);
            if (directLiveData != null) {
                directLiveData.postValue(direct);
            }
        }
    };

    public DeviceEarFindViewModel() {
        MediaPlayManager.getInstance().init();
    }

    public MutableLiveData<DeviceFindInfo> getDeviceFindInfoLiveData() {
        if (deviceFindInfoLiveData == null) {
            deviceFindInfoLiveData = new MutableLiveData<>();
        }
        model.setDeviceFindInfoListener(data -> deviceFindInfoLiveData.postValue(data));
        model.getDeviceFindInfo();
        return deviceFindInfoLiveData;
    }

    public MutableLiveData<Pair<Double, Double>> getUserLocationLiveData() {
        if (userLocationLiveData == null) {
            userLocationLiveData = new MutableLiveData<>();
        }
        return userLocationLiveData;
    }

    public MutableLiveData<Float> getDirectLiveData() {
        if (directLiveData == null) {
            directLiveData = new MutableLiveData<>();
        }
        return directLiveData;
    }

    public MutableLiveData<DeviceInfo> getCurrentDeviceLiveData() {
        return currentDeviceLiveData;
    }

    public void setCurrentDevice(String mac) {
        DeviceInfo deviceInfo = DeviceManager.getInstance().getDeviceInfo(mac);
        if (deviceInfo != null) {
            currentDeviceLiveData.postValue(deviceInfo);
        }
    }

    public void startLocation() {
        LocationManager.getInstance().startLocation(locationListener);
    }

    public void stopLocation() {
        LocationManager.getInstance().removeLocationListener(locationListener);
        LocationManager.getInstance().stopLocation();
    }

    public Pair<Double, Double> getDeviceLocation() {
        DeviceInfo deviceInfo = currentDeviceLiveData.getValue();
        return getPositionPair(deviceInfo);
    }

    @Nullable
    public Pair<Double, Double> getPositionPair(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return null;
        }
        String deviceLatitudeStr = deviceInfo.getLatitude();
        String deviceLongitudeStr = deviceInfo.getLongitude();
        if (StringUtil.isNullOrEmpty(deviceLongitudeStr) || StringUtil.isNullOrEmpty(deviceLatitudeStr)) {
            return null;
        }
        double deviceLatitude = Double.parseDouble(deviceLatitudeStr);
        double deviceLongitude = Double.parseDouble(deviceLongitudeStr);
        return new Pair<>(deviceLatitude, deviceLongitude);
    }

    public String getDeviceLocationText() {
        DeviceInfo deviceInfo = currentDeviceLiveData.getValue();
        return getLocationString(deviceInfo);
    }

    private static String getLocationString(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return StringUtil.EMPTY;
        }
        return deviceInfo.getLocation();
    }

    public String getDeviceLastRecordTime() {
        DeviceInfo deviceInfo = currentDeviceLiveData.getValue();
        return getDeviceLastRecordTime(deviceInfo);
    }

    @NonNull
    private static String getDeviceLastRecordTime(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return StringUtil.EMPTY;
        }
        return DateUtil.convertAbsoluteTime(deviceInfo.getLatestConnectTime());
    }

    public DeviceMarker getSingleDevice(String mac) {
        if (StringUtil.isNullOrEmpty(mac)) {
            return getDeviceMarker(DeviceManager.getInstance().getCurrentDeviceInfo());
        }else {
            return getDeviceMarker(DeviceManager.getInstance().getDeviceInfo(mac));
        }
    }

    @Nullable
    private DeviceMarker getDeviceMarker(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return null;
        }
        DeviceMarker deviceMarker = new DeviceMarker();
        Pair<Double, Double> positionPair = getPositionPair(deviceInfo);
        if (positionPair == null) {
            return null;
        }
        deviceMarker.setDeviceName(deviceInfo.getDeviceName());
        deviceMarker.setLatitude(positionPair.first);
        deviceMarker.setLongitude(positionPair.second);
        deviceMarker.setImageResId(deviceInfo.getIconResId());
        deviceMarker.setMac(deviceInfo.getMac());
        return deviceMarker;
    }

    public void setDeviceFindRing(DeviceFindState state) {
        model.setDeviceFindRing(DeviceSide.ALL, state, new DeviceEarFindModel.IResultCallback() {
            @Override
            public void onSuccess() {
                model.getDeviceFindInfo();
            }

            @Override
            public void onFail() {
                model.getDeviceFindInfo();
            }
        });
    }

    public boolean isConnected(String mac) {
        DeviceInfo currentDeviceInfo = DeviceManager.getInstance().getCurrentDeviceInfo();
        return currentDeviceInfo != null&& TextUtils.equals(currentDeviceInfo.getMac(),mac) && currentDeviceInfo.isConnected();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        model.onClean();
        stopLocation();
        MediaPlayManager.getInstance().stop();
    }
}
