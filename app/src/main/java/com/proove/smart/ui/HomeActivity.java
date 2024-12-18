package com.proove.smart.ui;

import static com.jieli.bluetooth.constant.StateCode.CONNECTION_CONNECTED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_OK;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hjq.permissions.XXPermissions;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.interfaces.rcsp.callback.BTRcspEventCallback;
import com.proove.ble.constant.BundleConstant;
import com.proove.ble.constant.DevicePopState;
import com.proove.ble.constant.Product;
import com.proove.ble.constant.SpConstant;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DeviceListItem;
import com.proove.ble.data.DevicePopInfo;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityDeviceListBinding;
import com.proove.smart.databinding.DialogLoadingBinding;
import com.proove.smart.manager.BleManager;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.model.DeviceListModel;
import com.proove.smart.model.DeviceScanModel;
import com.proove.smart.ui.adapter.DeviceListAdapter;
import com.proove.smart.ui.adapter.DeviceScanListAdapter;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.ui.dialog.LoadingDialogFragment;
import com.proove.smart.ui.dialog.ScanDeviceDialogFragment;
import com.proove.smart.vm.DeviceHighlanderViewModel;
import com.proove.smart.vm.DeviceListViewModel;
import com.proove.smart.vm.DevicePopViewModel;
import com.proove.smart.vm.DeviceScanViewModel;
import com.proove.smart.vm.DeviceViewModel;
import com.yscoco.lib.util.ActivityUtil;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.LocationUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity<ActivityDeviceListBinding> {
    private DeviceListAdapter recentDevicesAdapter, headphonesAdapter, smartwatchAdapter, electricScootersAdapter;
    private DeviceListViewModel viewModel;
    private final ScanDeviceDialogFragment scanDeviceDialogFragment = new ScanDeviceDialogFragment();
    private final ScanDeviceDialogFragment popDialog = new ScanDeviceDialogFragment();
    private final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();

    @Override
    protected ActivityDeviceListBinding getViewBinding() {
        return ActivityDeviceListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getDeviceList();
        startScan();
    }

    private void startScan() {
        String[] needGrantPermission = getNeedGrantPermission();
        if (needGrantPermission.length == 0 && DeviceListViewModel.isDevicePopupOn()) {
            viewModel.startScan();
        }else {
            viewModel.stopScan();
        }
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(DeviceListViewModel.class);
        recentDevicesAdapter = new DeviceListAdapter();
        headphonesAdapter = new DeviceListAdapter();
        smartwatchAdapter = new DeviceListAdapter();
        electricScootersAdapter = new DeviceListAdapter();

        binding.rvRecent.setHasFixedSize(true);
        binding.rvHeadphones.setHasFixedSize(true);
        binding.rvSmartwatch.setHasFixedSize(true);
        binding.rvElectric.setHasFixedSize(true);

        binding.rvRecent.setAdapter(recentDevicesAdapter);
        binding.rvHeadphones.setAdapter(headphonesAdapter);
        binding.rvSmartwatch.setAdapter(smartwatchAdapter);
        binding.rvElectric.setAdapter(electricScootersAdapter);
    }

    @Override
    protected void initData() {
        viewModel.getDeviceList();
        viewModel.getDeviceListLiveData().observe(this, this::setRecentList);
        viewModel.getScanListLiveData().observe(this, this::onChanged);
        viewModel.getDevicePopInfoLiveData().observe(this, this::setPopDialog);
        scanDeviceDialogFragment.getDialogState().observe(this, b -> {
            if (b) {
                viewModel.scan();
            } else {
                viewModel.stop();
            }
        });
    }

    private void setPopDialog(DevicePopInfo devicePopInfo) {
        DeviceInfo deviceInfo = devicePopInfo.getDeviceInfo();
        if (deviceInfo != null && !viewModel.isInnerConnect(deviceInfo.getMac()) &&
                !viewModel.isAlreadyInDeviceList(deviceInfo.getMac())) {
            List<DeviceListItem> list = new ArrayList<>();
            DeviceListItem item = new DeviceListItem();
            item.setMac(deviceInfo.getMac());
            item.setDeviceName(deviceInfo.getDeviceName());
            list.add(item);
            popDialog.setListItems(list);
            showPopDialog();
        }

    }

    private void onChanged(List<DeviceListItem> data) {
        Log.i(TAG, "onChanged: "+data.size());
        scanDeviceDialogFragment.getScanListAdapter().submitList(data);
    }

    private void setRecentList(List<DeviceListItem> data) {
        recentDevicesAdapter.submitList(data);

        List<DeviceListItem> headphonesListItems = new ArrayList<>();
        for (DeviceListItem deviceListItem : data) {
            if (!deviceListItem.equalsEarDevice()) {
                continue;
            }
            headphonesListItems.add(deviceListItem);
        }
        headphonesAdapter.submitList(headphonesListItems);

        List<DeviceListItem> electricScootersListItems = new ArrayList<>();
        for (DeviceListItem deviceListItem : data) {
            if (!deviceListItem.equalsDevice()) {
                continue;
            }
            electricScootersListItems.add(deviceListItem);
        }
        electricScootersAdapter.submitList(electricScootersListItems);

//        List<DeviceListItem> smartwatchListItems = new ArrayList<>();
//        DeviceListItem item = new DeviceListItem(100, "IWatch", R.drawable.image_product_watch);
//        smartwatchListItems.add(item);
//        smartwatchAdapter.submitList(smartwatchListItems);
    }

    @Override
    protected void initListener() {
        binding.ivAdd.setOnClickListener(v -> openScan());
        recentDevicesAdapter.setOnItemClickListener(this::clickDevice);
        headphonesAdapter.setOnItemClickListener(this::clickDevice);
        electricScootersAdapter.setOnItemLongClickListener(this::clickDevice);
    }

    private void openScan() {
        if (!BluetoothUtil.isBluetoothEnable()) {
            showOpenBluetoothTip();
            return;
        }
        if (!LocationUtil.isLocationOpen(this)) {
            showOpenLocationTip();
            return;
        }
        if (scanDeviceDialogFragment.isAdded()) {
            return;
        }
        String[] needGrantPermission = getNeedGrantPermission();
        if (needGrantPermission.length > 0) {
            showPermissionDialog(
                    () -> XXPermissions.with(HomeActivity.this)
                            .permission(needGrantPermission)
                            .request((permissions, allGranted) -> {
                                if (allGranted) {
                                    showScanDialog();
                                }
                            }),
                    scanDeviceDialogFragment::dismiss);
            return;
        }
        showScanDialog();
    }

    private void showScanDialog() {
        if (scanDeviceDialogFragment.isAdded()) {
            return;
        }
        scanDeviceDialogFragment.setOnDialogConfirmClick(this::clickScannedDevice);
        scanDeviceDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void showPopDialog() {
        if (popDialog.isAdded()) {
            return;
        }
        popDialog.setOnDialogConfirmClick(this::clickScannedDevice);
        popDialog.show(getSupportFragmentManager(), TAG);
    }

    private void clickScannedDevice(DeviceListItem data) {
        if (data.equalsDevice()) {
            highlander(data);
            return;
        }
        if (data.equalsEarDevice()) {
            connectDevice(data.getMac(), data.getDeviceName());
        }
    }

    private void clickDevice(DeviceListItem data) {
        if (data.equalsDevice()) {
            if (data.deviceInfo.isConnected()) {
                highlander(data);
            } else {
                DeviceManager.getInstance().setCurrentMac(data.getMac());
                DeviceHighlanderInfoActivity.start(data.getDeviceName(), data.getMac(), HomeActivity.this);
            }
            return;
        }
        if (data.equalsEarDevice()) {
            if (data.deviceInfo.isConnected()) {
                connectDevice(data.getMac(), data.getDeviceName());
            } else {
                DeviceManager.getInstance().setCurrentMac(data.getMac());
                startActivity(data.getMac(), data.getDeviceName(), DeviceEarInfoActivity.class);
            }

        }
    }

    private void highlander(DeviceListItem data) {
        if (!BluetoothUtil.isBluetoothEnable()) {
            showOpenBluetoothTip();
            return;
        }
        loadingDialogFragment.show(getSupportFragmentManager(), TAG);
        viewModel.connectBle(data.getMac(), new DeviceScanModel.IResultCallBack() {
            @Override
            public void onSuccess() {
                loadingDialogFragment.dismiss();
                DeviceManager.getInstance().getLocation(data.getMac());
                DeviceHighlanderInfoActivity.start(data.getDeviceName(), data.getMac(), HomeActivity.this);
            }

            @Override
            public void onFail() {
                loadingDialogFragment.dismiss();
                ToastUtil.showToast(HomeActivity.this,
                        getString(R.string.device_connect_fail));
            }
        });
    }

    public void connectDevice(String mac, String deviceName) {
        if (!BluetoothUtil.isBluetoothEnable()) {
            showOpenBluetoothTip();
            return;
        }
        if (mac.isEmpty()) {
            ToastUtil.showToast(this, "mac is null");
            return;
        }
        loadingDialogFragment.show(getSupportFragmentManager(), TAG);

        viewModel.connectDevice(mac, new DeviceScanModel.IResultCallBack() {
            @Override
            public void onSuccess() {
                loadingDialogFragment.dismiss();
                startActivity(mac, deviceName, DeviceEarInfoActivity.class);
            }

            @Override
            public void onFail() {
                loadingDialogFragment.dismiss();
                ToastUtil.showToast(HomeActivity.this,
                        getString(R.string.device_connect_fail));
            }
        });

    }

    @Override
    protected void onDestroy() {
        DeviceManager.getInstance().onClean();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.release();
        if (scanDeviceDialogFragment != null && scanDeviceDialogFragment.isVisible()) {
            scanDeviceDialogFragment.dismiss();
        }
    }
}
