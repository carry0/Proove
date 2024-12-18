package com.proove.smart.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.proove.ble.constant.Product;
import com.proove.ble.data.BluetoothDataParser;
import com.proove.smart.BuildConfig;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityDeviceHighlanderInfoBinding;
import com.proove.smart.databinding.ActivityDeviceHighlanderInfoDetailBinding;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.ui.dialog.EditDialogFragment;
import com.proove.smart.vm.DeviceHighlanderViewModel;

public class DeviceHighlanderDetailInfoActivity extends BaseActivity<ActivityDeviceHighlanderInfoDetailBinding> {
    private DeviceHighlanderViewModel viewModel;
    private final EditDialogFragment editDialogFragment = new EditDialogFragment();
    String name;
    String mac;

    public static void start(String name, String mac, Activity activity) {
        Intent intent = new Intent(activity, DeviceHighlanderDetailInfoActivity.class);
        intent.putExtra("DeviceName", name);
        intent.putExtra("DeviceMac", mac);
        activity.startActivity(intent);
    }

    @Override
    protected ActivityDeviceHighlanderInfoDetailBinding getViewBinding() {
        return ActivityDeviceHighlanderInfoDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        Product product  = DeviceManager.getInstance().getCurrentProduct();
        if (product!=null){
            binding.image.setImageResource(product.getProductImage());
        }
        mac = getIntent().getStringExtra("DeviceMac");
        name = getIntent().getStringExtra("DeviceName");
        viewModel = new ViewModelProvider(this).get(DeviceHighlanderViewModel.class);
        viewModel.getStatusMutableLiveData().observe(this, this::upDataUI);
        viewModel.getConnectStatusLiveData().observe(this, is -> {
            if (is) {
                return;
            }
            finish();
        });
        binding.tvTitle.setText(name);
        binding.tvAppVersion.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));
    }

    private void upDataUI(BluetoothDataParser.DeviceStatus deviceStatus) {
        binding.tvSpeed.setText(getString(!deviceStatus.isMileUnit?R.string.km_h:R.string.mi_h, deviceStatus.speed));
        binding.batteryLevel.setProgress(deviceStatus.batteryLevel);
        binding.batteryLevel.setProgressDrawable(getDrawableBg(deviceStatus.batteryLevel));
        binding.totalMileage.setText(getString(!deviceStatus.isMileUnit?R.string.km:R.string.mi, deviceStatus.totalMileage));
        binding.batteryPercentage.setText(getString(R.string.battery_, deviceStatus.batteryPercentage));
        binding.controllerTemp.setText(getString(R.string.controller_temp, deviceStatus.controllerTemp));
        binding.firmwareVersion.setText(deviceStatus.firmwareVersion);
    }

    private Drawable getDrawableBg(int battery) {
        return battery > 2 ?
                ContextCompat.getDrawable(this, R.drawable.progress_bar_background_1) :
                ContextCompat.getDrawable(this, R.drawable.progress_bar_background_2);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        binding.ivChangeNameDialog.setOnClickListener(view -> showEditDialog());
        binding.clOtaUpdate.setOnClickListener(view -> DeviceOtaUpdateActivity.start(name, mac, this));
        binding.ivSetting.setOnClickListener(view -> ScooterSettingsActivity.start(name, mac, this));
    }

    private void showEditDialog() {
        editDialogFragment.setTitle(getString(R.string.change_the_name_of_the_scooter));
        editDialogFragment.setLastEditText(binding.tvTitle.getText().toString());
        editDialogFragment.setOnDialogClick(this::saveName);
        editDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void saveName(String str) {
        binding.tvTitle.setText(str);
        DeviceManager.getInstance().saveDeviceName(mac, str);
    }
}
