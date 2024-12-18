package com.proove.smart.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.proove.ble.constant.DevicePopState;
import com.proove.ble.constant.SpConstant;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DeviceListItem;
import com.proove.ble.data.DevicePopInfo;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.model.DeviceListModel;
import com.proove.smart.model.DeviceListModelImpl;
import com.proove.smart.model.DevicePopModel;
import com.proove.smart.model.DevicePopModelImpl;
import com.proove.smart.model.DeviceScanModel;
import com.proove.smart.model.DeviceScanModelImpl;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.SpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceListViewModel extends ViewModel {
    private static final String TAG = "DeviceListViewModel";
    private final MutableLiveData<List<DeviceListItem>> scanListLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<DeviceListItem>> deviceListLiveData = new MutableLiveData<>();
    private MutableLiveData<DevicePopInfo> devicePopInfoLiveData;
    private MutableLiveData<Boolean> connectStateLiveData;
    private final DeviceListModel model;
    private final DeviceScanModel scanModel;
    private final DevicePopModel popModel;

    public MutableLiveData<List<DeviceListItem>> getScanListLiveData() {
        return scanListLiveData;
    }

    public void scan() {
        scanModel.startBleScan(deviceInfoList -> scanListLiveData.setValue(getItems(deviceInfoList)));
    }

    public void stop() {
        scanModel.stopBleScan();
    }

    public MutableLiveData<List<DeviceListItem>> getDeviceListLiveData() {
        return deviceListLiveData;
    }
    public void getConnectStatus() {
        model.getConnectStatus();
    }
    public DeviceListViewModel() {
        scanModel = new DeviceScanModelImpl();
        model = new DeviceListModelImpl();
        popModel = new DevicePopModelImpl();
    }

    public MutableLiveData<Boolean> getConnectStateLiveData() {
        if (connectStateLiveData == null) {
            connectStateLiveData = new MutableLiveData<>();
            model.setConnectStatusListener(isConnected -> connectStateLiveData.postValue(isConnected));
        }
        return connectStateLiveData;
    }

    public MutableLiveData<DevicePopInfo> getDevicePopInfoLiveData() {
        if (devicePopInfoLiveData == null) {
            devicePopInfoLiveData = new MutableLiveData<>();
            if (!isDevicePopupOn()) {
                return devicePopInfoLiveData;
            }
            popModel.setDevicePopInfoListener(data -> {
                devicePopInfoLiveData.postValue(data);
            });
        }
        return devicePopInfoLiveData;
    }

    public static boolean isDevicePopupOn() {
        int value = SpUtil.getInstance().getInt(SpConstant.AUTO_POPUP_STATE, DevicePopState.OFF.getValue());
        DevicePopState devicePopState = DevicePopState.valueOf(value);
        return devicePopState == DevicePopState.ON;
    }

    public static void setDevicePopState(DevicePopState devicePopState) {
        if (devicePopState == null) {
            return;
        }
        SpUtil.getInstance().putInt(SpConstant.AUTO_POPUP_STATE, devicePopState.getValue());
    }

    public void startScan() {
        if (isDevicePopupOn()) {
            popModel.startScan();
        }
    }

    public void stopScan() {
        if (!isDevicePopupOn()) {
            popModel.stopScan();
        }
    }

    public boolean isInnerConnect(String mac) {
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return false;
        }
        return RCSPController.getInstance().isDeviceConnected(BluetoothUtil.getBluetoothDevice(mac));
    }

    public boolean isAlreadyInDeviceList(String mac) {
        List<DeviceInfo> deviceInfoList = DeviceManager.getInstance().getDeviceInfoList();
        for (DeviceInfo deviceInfo : deviceInfoList) {
            return deviceInfo.getMac().equals(mac);
        }
        return false;
    }

    public void getDeviceList() {
        model.getDeviceInfoList(deviceInfoList -> {
            List<DeviceListItem> deviceListItems = new ArrayList<>();
            for (DeviceInfo deviceInfo : deviceInfoList) {
                DeviceListItem deviceListItem = new DeviceListItem(deviceInfo);
                deviceListItems.add(deviceListItem);
            }
            deviceListLiveData.postValue(dataCollectors(deviceListItems));
        });
    }

    public void connectDevice(String mac, DeviceScanModel.IResultCallBack connectCallBack) {
        scanModel.connectDevice(mac, connectCallBack);
    }
    public void connectBle(String mac, DeviceScanModel.IResultCallBack connectCallBack) {
        scanModel.connectBle(mac, connectCallBack);
    }
    private List<DeviceListItem> dataCollectors(List<DeviceListItem> list) {
        return list.stream().sorted((o1, o2) -> {
            int o1P = o1.isConnected() ? 1 : 0;
            int o2P = o2.isConnected() ? 1 : 0;
            if (o1P == o2P) {
                return Long.compare(o2.getAddedTime(), o1.getAddedTime());
            }
            return o2P - o1P;
        }).collect(Collectors.toList());
    }

    public void deleteDevice(String mac) {
        model.deleteDevice(mac);
    }

    @NonNull
    private List<DeviceListItem> getItems(List<DeviceInfo> deviceInfoList) {
        List<DeviceListItem> deviceListItems = new ArrayList<>();
        for (DeviceInfo deviceInfo : deviceInfoList) {
            DeviceListItem deviceListItem = new DeviceListItem(deviceInfo);
            deviceListItem.setProductImageResId(deviceInfo.getImageResId());
            deviceListItems.add(deviceListItem);
        }
        return deviceListItems;
    }

    public void release(){
        scanModel.release();
        popModel.release();
    }
    @Override
    public void onCleared() {
        super.onCleared();
        model.onClean();
    }
}
