package com.proove.smart.ui;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.proove.ble.constant.Product;
import com.proove.ble.data.BluetoothDataParser;
import com.proove.smart.BuildConfig;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityDeviceHighlanderBatteryStatusBinding;
import com.proove.smart.databinding.ActivityDeviceHighlanderInfoDetailBinding;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.vm.DeviceHighlanderViewModel;

public class DeviceHighlanderBatteryStatusActivity extends BaseActivity<ActivityDeviceHighlanderBatteryStatusBinding> {
    private DeviceHighlanderViewModel viewModel;
    public static void start(String name, String mac, Activity activity) {
        Intent intent = new Intent(activity, DeviceHighlanderBatteryStatusActivity.class);
        intent.putExtra("DeviceName", name);
        intent.putExtra("DeviceMac", mac);
        activity.startActivity(intent);
    }
    @Override
    protected ActivityDeviceHighlanderBatteryStatusBinding getViewBinding() {
        return ActivityDeviceHighlanderBatteryStatusBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(DeviceHighlanderViewModel.class);
        viewModel.getStatusMutableLiveData().observe(this, this::upDataUI);
        viewModel.getConnectStatusLiveData().observe(this, is -> {
            if (is) {
                return;
            }
            finish();
        });
        binding.tvAppVersion.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));
    }
    private void upDataUI(BluetoothDataParser.DeviceStatus deviceStatus) {
        binding.firmwareVersion.setText(deviceStatus.firmwareVersion);
        binding.batteryPercentage.setText(getString(R.string.battery_, deviceStatus.batteryPercentage));
        binding.tvBattery.setText(getString(R.string.battery_, deviceStatus.batteryPercentage));
        binding.batteryVoltage.setText(getString(R.string.battery_voltage, deviceStatus.batteryVoltage));
        binding.batteryProgressView.setProgress(deviceStatus.batteryPercentage);
        binding.constraintLayout10.setVisibility(deviceStatus.batteryLevel>2? View.GONE:View.VISIBLE);
        binding.textView27.setVisibility(deviceStatus.batteryLevel>2? View.GONE:View.VISIBLE);
    }
    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        binding.ivBack.setOnClickListener(view -> finish());
    }
}
