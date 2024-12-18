package com.proove.smart.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.jieli.bluetooth.tool.DeviceAddrManager;
import com.proove.ble.constant.SpConstant;
import com.proove.ble.data.BleAdvMsg;
import com.proove.ble.data.BluetoothDataBuilder;
import com.proove.ble.data.BluetoothDataParser;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityPrivacyPolicyBinding;
import com.proove.smart.databinding.ActivityScooterSettingsBinding;
import com.proove.smart.manager.BleManager;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.ui.dialog.MsgDialogFragment;
import com.proove.smart.ui.dialog.PickerDialogFragment;
import com.proove.smart.vm.DeviceHighlanderViewModel;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.MultiLanguageUtils;

import java.util.List;
import java.util.Locale;

public class ScooterSettingsActivity extends BaseActivity<ActivityScooterSettingsBinding> {

    private DeviceHighlanderViewModel viewModel;
    private final PickerDialogFragment pickerDialogFragment = new PickerDialogFragment();
    String mac;
    private final MsgDialogFragment msgDialogFragment = new MsgDialogFragment();
    private List<String> energyRecoveryList;

    public static void start(String name, String mac, Activity activity) {
        Intent intent = new Intent(activity, ScooterSettingsActivity.class);
        intent.putExtra("DeviceName", name);
        intent.putExtra("DeviceMac", mac);
        activity.startActivityForResult(intent,1001);
    }

    @Override
    protected ActivityScooterSettingsBinding getViewBinding() {
        return ActivityScooterSettingsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mac = getIntent().getStringExtra("DeviceMac");
        energyRecoveryList = List.of(getString(R.string.low), getString(R.string.medium), getString(R.string.high));
        viewModel = new ViewModelProvider(this).get(DeviceHighlanderViewModel.class);
        viewModel.getStatusMutableLiveData().observe(this, this::upDataUI);
        viewModel.getConnectStatusLiveData().observe(this, is -> {
            if (is) {
                return;
            }
            finish();
        });
        if ( viewModel.getConnectStatusLiveData().getValue()==null){
            binding.swCruiseControl.setEnabled(false);
            binding.swWalking.setEnabled(false);
            binding.clEnergy.setEnabled(false);
            binding.swCruiseControl.setEnabled(false);
            binding.swBritish.setEnabled(false);
        }
    }

    private void upDataUI(BluetoothDataParser.DeviceStatus deviceStatus) {
        binding.swCruiseControl.setChecked(deviceStatus.cruiseEnabled);
        binding.swWalking.setChecked(deviceStatus.brakeMode == 1);
        binding.tvEnergyRecovery.setText(deviceStatus.brakeMode == 2 ? getText(R.string.medium) : deviceStatus.brakeMode == 3 ? getText(R.string.high) : getText(R.string.low));
        binding.swBritish.setChecked(deviceStatus.isMileUnit);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        binding.ivBack.setOnClickListener(view -> finish());
        binding.clEnergy.setOnClickListener(view -> showEnergyRecoveryDialog());
        binding.swCruiseControl.setOnClickListener(view -> BleManager.getInstance().writeCommonData(BluetoothDataBuilder.setCruiseMode(binding.swCruiseControl.isChecked())));
        binding.swBritish.setOnClickListener(view -> BleManager.getInstance().writeCommonData(BluetoothDataBuilder.setUnit(binding.swBritish.isChecked())));
        binding.swWalking.setOnClickListener(view -> BleManager.getInstance().writeCommonData(BluetoothDataBuilder.setDriveMode(binding.swWalking.isChecked() ? 1 : 2)));
        binding.clRemoveDevice.setOnClickListener(view -> showRemoveDeviceDialog());
    }

    private void showEnergyRecoveryDialog() {
        int position;
        if (TextUtils.equals(binding.tvEnergyRecovery.getText().toString(), getString(R.string.high))) {
            position = 2;
        } else if (TextUtils.equals(binding.tvEnergyRecovery.getText().toString(), getString(R.string.medium))) {
            position = 1;
        } else {
            position = 0;
        }
        pickerDialogFragment.setTitle(getString(R.string.energy_recovery));
        pickerDialogFragment.setData(energyRecoveryList);
        pickerDialogFragment.setDefaultPosition(position);
        pickerDialogFragment.setSelectedListener(this::energySwitch);
        pickerDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void energySwitch(int i, String s) {
        BleManager.getInstance().writeCommonData(BluetoothDataBuilder.setBrakeMode(i + 1));
    }

    private void showRemoveDeviceDialog() {
        msgDialogFragment.setTitle(getString(R.string.remove_device) + "?");
        msgDialogFragment.setMsg(getString(R.string.are_you_sure_you_want_to_remove_the_device_from_the_proove_app));
        msgDialogFragment.setConfirmText(getString(R.string.remove));
        msgDialogFragment.setCancelText(getString(R.string.cancel));
        msgDialogFragment.setOnDialogConfirmClick(() -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                DeviceManager.getInstance().removeDevice(mac);
                BleManager.getInstance().disconnectGatt(mac);
                DeviceAddrManager.getInstance().removeHistoryBluetoothDevice(mac);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("DeviceDelete", "deleteDevice");
                setResult(RESULT_OK, resultIntent);
                finish();
            }, 500);
        });
        msgDialogFragment.show(getSupportFragmentManager(), TAG);
    }
}
