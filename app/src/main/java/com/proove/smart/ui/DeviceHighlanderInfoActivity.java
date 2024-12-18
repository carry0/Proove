package com.proove.smart.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.google.gson.Gson;
import com.proove.ble.constant.Constant;
import com.proove.ble.constant.Product;
import com.proove.ble.data.BluetoothDataParser;
import com.proove.ble.data.SimpleLocation;
import com.proove.smart.BuildConfig;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityDeviceHighlanderInfoBinding;
import com.proove.smart.manager.BleManager;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.manager.LocationManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.ui.dialog.EditDialogFragment;
import com.proove.smart.ui.dialog.LoadingDialogFragment;
import com.proove.smart.vm.DeviceHighlanderViewModel;

import java.util.List;

public class DeviceHighlanderInfoActivity extends BaseActivity<ActivityDeviceHighlanderInfoBinding> {
    private DeviceHighlanderViewModel viewModel;
    private final EditDialogFragment editDialogFragment = new EditDialogFragment();
    private String mac;
    String name;
    private static final String EXTRA_DELETE_DEVICE = "DeviceDelete";
    String TAG = "DeviceHighlanderInfoActivity";

    private final BroadcastReceiver bleStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device==null){
                    return;
                }
                String deviceAddress = device.getAddress();
                if (mac.equals(deviceAddress)) {
                      disableViews();
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device==null){
                    return;
                }
                String deviceAddress = device.getAddress();
                if (mac.equals(deviceAddress)) {
                    enableViews();
                }
            }
        }
    };
    public static void start(String name, String mac, Activity activity) {
        Intent intent = new Intent(activity, DeviceHighlanderInfoActivity.class);
        intent.putExtra("DeviceName", name);
        intent.putExtra("DeviceMac", mac);
        activity.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
//        BleManager.getInstance().disconnectGatt(mac);
//        LocationManager.getInstance().stopTracking();
        super.onBackPressed();
    }


    @Override
    protected ActivityDeviceHighlanderInfoBinding getViewBinding() {
        return ActivityDeviceHighlanderInfoBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bleStateReceiver, filter);
        Product product  =DeviceManager.getInstance().getCurrentProduct();
        if (product!=null){
            binding.image.setImageResource(product.getProductImage());
        }
        mac = getIntent().getStringExtra("DeviceMac");
        name = getIntent().getStringExtra("DeviceName");
        viewModel = new ViewModelProvider(this).get(DeviceHighlanderViewModel.class);
        viewModel.getStatusMutableLiveData().observe(this, this::upDataUI);
        binding.tvTitle.setText(name);
        binding.tvAppVersion.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));
    }
    private void disableViews() {
        runOnUiThread(() -> {
            BluetoothDataParser.DeviceStatus deviceStatus = new BluetoothDataParser.DeviceStatus();
            deviceStatus.isMileUnit = false;
            deviceStatus.batteryLevel = 0;
            deviceStatus.speed = 0;
            deviceStatus.totalMileage = 0;
            deviceStatus.batteryPercentage = 0;
            upDataUI(deviceStatus);
            binding.llPlayStop.setEnabled(false);
            binding.ivChangeNameDialog.setEnabled(false);
            binding.clAboutTheDevice.setEnabled(false);
            binding.clBattery.setEnabled(false);
            binding.clTimer.setEnabled(false);
            binding.imageView10.setSelected(false);
        });
    }

    private void enableViews() {
        runOnUiThread(() -> {
            binding.llPlayStop.setEnabled(true);
            binding.ivChangeNameDialog.setEnabled(true);
            binding.clAboutTheDevice.setEnabled(true);
            binding.clBattery.setEnabled(true);
            binding.clTimer.setEnabled(true);
            binding.imageView10.setSelected(true);
        });

    }
    private void upDataUI(BluetoothDataParser.DeviceStatus deviceStatus) {
        binding.tvSpeed.setText(getString(!deviceStatus.isMileUnit?R.string.km_h:R.string.mi_h, deviceStatus.speed));
        binding.batteryLevel.setProgress(deviceStatus.batteryLevel);
        binding.batteryLevel.setProgressDrawable(getDrawableBg(deviceStatus.batteryLevel));
        binding.totalMileage.setText(getString(!deviceStatus.isMileUnit?R.string.km:R.string.mi, deviceStatus.totalMileage));
        binding.batteryPercentage.setText(getString(R.string.battery_, deviceStatus.batteryPercentage));
    }

    private Drawable getDrawableBg(int battery) {
        return battery > 2 ?
                ContextCompat.getDrawable(this, R.drawable.progress_bar_background_1) :
                ContextCompat.getDrawable(this, R.drawable.progress_bar_background_2);
    }

    @Override
    protected void initData() {
        LocationManager.getInstance().addLocationListener(new LocationManager.ILocationListener() {
            @Override
            public void onLocationError(String errorMessage) {
                super.onLocationError(errorMessage);
                Log.i(TAG, "onLocationError: "+errorMessage);
            }

            @Override
            public void onTrackUpdate(List<SimpleLocation> track) {
                super.onTrackUpdate(track);
                Log.i(TAG, "onTrackUpdate: "+track);
                for (SimpleLocation point : track) {
                    Log.d(TAG, "位置: " + point.getAddressStr() +
                            " 经度: " + point.getLongitude() +
                            " 纬度: " + point.getLatitude());
                }
            }
        });
    }

    @Override
    protected void initListener() {
        binding.ivBack.setOnClickListener(view -> onBackPressed());
        binding.llPlayStop.setOnClickListener(view -> saveLocation());
        binding.ivChangeNameDialog.setOnClickListener(view -> showEditDialog());
        binding.llNavigation.setOnClickListener(view -> DeviceHighlanderFindActivity.start(name, mac, this));
        binding.ivSetting.setOnClickListener(view -> ScooterSettingsActivity.start(name, mac, this));
        binding.clMoreSetting.setOnClickListener(view -> DeviceMoreSettingActivity.start(name, mac, this));
        binding.clAboutTheDevice.setOnClickListener(view -> DeviceHighlanderDetailInfoActivity.start(name, mac, this));
        binding.clBattery.setOnClickListener(view -> DeviceHighlanderBatteryStatusActivity.start(name, mac, this));
        binding.clTimer.setOnClickListener(view -> TravelHistoryActivity.start(name, mac, this));
    }

    private void saveLocation() {
        if (binding.llPlayStop.isSelected()) {
            binding.ivStart.setSelected(false);
//            binding.llFinishTheTrip.setVisibility(View.VISIBLE);
            binding.tvStart.setText(R.string.start_moving);
            LocationManager.getInstance().stopLocation();
        } else {
            binding.ivStart.setSelected(true);
            binding.tvStart.setText(R.string.suspend_the_trip);
            LocationManager.getInstance().startTracking();
        }
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String result = data.getStringExtra(EXTRA_DELETE_DEVICE);
            if (result != null && result.equals("deleteDevice")) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 取消注册广播接收器
        unregisterReceiver(bleStateReceiver);
        super.onDestroy();
    }
}
