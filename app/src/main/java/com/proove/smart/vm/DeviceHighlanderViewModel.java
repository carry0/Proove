package com.proove.smart.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.proove.ble.constant.DevicePopState;
import com.proove.ble.constant.SpConstant;
import com.proove.ble.data.BluetoothDataParser;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DeviceListItem;
import com.proove.ble.data.DevicePopInfo;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.model.DeviceListModel;
import com.proove.smart.model.DeviceListModelImpl;
import com.proove.smart.model.DeviceModelImpl;
import com.proove.smart.model.DevicePopModel;
import com.proove.smart.model.DevicePopModelImpl;
import com.proove.smart.model.DeviceScanModel;
import com.proove.smart.model.DeviceScanModelImpl;
import com.proove.smart.model.Model;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.SpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceHighlanderViewModel extends ViewModel {
    private static final String TAG = "DeviceListViewModel";

    private final Model model = new DeviceModelImpl();
    private MutableLiveData<Boolean> connectStatusLiveData;
    private MutableLiveData<BluetoothDataParser.DeviceStatus> statusMutableLiveData;

    public MutableLiveData<BluetoothDataParser.DeviceStatus> getStatusMutableLiveData() {
        if (statusMutableLiveData==null){
            statusMutableLiveData = new MutableLiveData<>();
            model.setDeviceStatusIDataCallback(data -> statusMutableLiveData.postValue(data));
        }
        return statusMutableLiveData;
    }

    public MutableLiveData<Boolean> getConnectStatusLiveData() {
        if (connectStatusLiveData == null) {
            connectStatusLiveData = new MutableLiveData<>();
            model.setConnectStatusListener(data -> connectStatusLiveData.postValue(data != null && data));
        }
        return connectStatusLiveData;
    }
}
