package com.proove.smart.ui;

import static com.jieli.bluetooth.constant.StateCode.CONNECTION_CONNECTED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_FAILED;
import static com.jieli.bluetooth.constant.StateCode.CONNECTION_OK;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.jieli.bluetooth.interfaces.rcsp.callback.BTRcspEventCallback;
import com.proove.ble.constant.AncType;
import com.proove.ble.constant.BatteryType;
import com.proove.ble.constant.BundleConstant;
import com.proove.ble.constant.DevicePopState;
import com.proove.ble.constant.Product;
import com.proove.ble.constant.SpConstant;
import com.proove.ble.constant.protocol.AncMode;
import com.proove.ble.constant.protocol.WorkMode;
import com.proove.ble.data.BatteryInfo;
import com.proove.ble.data.DeviceInfo;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityDeviceEarInfoBinding;
import com.proove.smart.manager.BluetoothManager;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.ui.dialog.EditDialogFragment;
import com.proove.smart.ui.dialog.MsgDialogFragment;
import com.proove.smart.vm.DeviceEarInfoViewModel;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.ToastUtil;

public class DeviceEarInfoActivity extends BaseActivity<ActivityDeviceEarInfoBinding> {
    private static final int REQUEST_CODE_MORE_SETTINGS = 1001;
    private static final String EXTRA_DELETE_DEVICE = "key";
    private DeviceEarInfoViewModel viewModel;
    private final MsgDialogFragment msgDialogFragment = new MsgDialogFragment();
    private final EditDialogFragment editDialogFragment = new EditDialogFragment();
    private String mac;

    @Override
    protected ActivityDeviceEarInfoBinding getViewBinding() {
        return ActivityDeviceEarInfoBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mac = getIntent().getStringExtra(BundleConstant.MAC_KEY);
        viewModel = new ViewModelProvider(this).get(DeviceEarInfoViewModel.class);
        binding.setLifecycleOwner(this);

        binding.skViewL.setEnabled(false);
        binding.skViewR.setEnabled(false);
        binding.skViewAll.setEnabled(false);

        Product product = DeviceManager.getInstance().getCurrentProduct();
        if (product == null) {
            return;
        }
        if (product.getProductIconEarL() != 0) {
            binding.ivEarL.setImageResource(product.getProductIconEarL());
            binding.ivEarR.setImageResource(product.getProductIconEarR());
        }
        binding.textView11.setText(product.getProductId() == 0x000B || product.getProductId() == 0x000F ? "C" : "L");
        binding.llR.setVisibility(product.getProductId() == 0x000B || product.getProductId() == 0x000F ? View.GONE : View.VISIBLE);
        binding.clChangeName.setVisibility(product.getProductId() == 0x000B || product.getProductId() == 0x000F ? View.GONE : View.VISIBLE);
        binding.clKeySetting.setVisibility(product.getProductId() == 0x000B || product.getProductId() == 0x000F ? View.GONE : View.VISIBLE);
        binding.ll3d.setVisibility(product.getProductId() == 0x000B || product.getProductId() == 0x000F ? View.VISIBLE : View.GONE);
        binding.llAll.setVisibility(product.getBatteryType() != BatteryType.DOUBLE ? View.VISIBLE : View.GONE);
        binding.ll3dMode.setVisibility(product.isSupport3DMode() ? View.VISIBLE : View.GONE);
        binding.llAnc.setVisibility(product.getAncType() == AncType.NOT_SUPPORT ? View.GONE : View.VISIBLE);
        binding.llTransparency.setVisibility(product.getAncType() == AncType.NOT_SUPPORT ? View.GONE : View.VISIBLE);
        binding.llTurnOff.setVisibility(product.getAncType() == AncType.NOT_SUPPORT ? View.GONE : View.VISIBLE);
        binding.clOtaUpdate.setVisibility(product.isSupportOta() ? View.VISIBLE : View.GONE);
        String name = getIntent().getStringExtra(BundleConstant.TITLE);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.llDeviceSearch.getLayoutParams();
        float density = getResources().getDisplayMetrics().density;
        if (binding.llAnc.getVisibility() == View.VISIBLE) {
            params.topMargin = (int) (8 * density);
            binding.llDeviceSearch.setLayoutParams(params);
        } else {
            params.topMargin = 0;
            binding.llDeviceSearch.setLayoutParams(params);
        }
        ConstraintLayout.LayoutParams ll3dModeLayoutParams = (ConstraintLayout.LayoutParams) binding.ll3dMode.getLayoutParams();
        ConstraintLayout.LayoutParams llTurnOffLayoutParams = (ConstraintLayout.LayoutParams) binding.llTurnOff.getLayoutParams();
        if (binding.ll3dMode.getVisibility() == View.VISIBLE && binding.llTurnOff.getVisibility() == View.VISIBLE) {
            ll3dModeLayoutParams.leftMargin = (int) (8 * density);
            llTurnOffLayoutParams.leftMargin = 0;
        } else {
            ll3dModeLayoutParams.leftMargin = (int) (16 * density);
            llTurnOffLayoutParams.leftMargin = (int) (8 * density);
        }
        binding.ll3dMode.setLayoutParams(ll3dModeLayoutParams);
        binding.llTurnOff.setLayoutParams(llTurnOffLayoutParams);
        binding.tvTitle.setText(name == null || name.isEmpty() ? "" : name);
    }

    @Override
    protected void initData() {
        viewModel.getConnectStateLiveData().observe(this, this::upDateUI);
        viewModel.getBatteryLiveData().observe(this, this::initBatteryUI);
        viewModel.getAncModeLiveData().observe(this, this::initANCUI);
        viewModel.getWorkModeLiveData().observe(this, workMode -> binding.llGameMode.setSelected(workMode == WorkMode.GAME));
        viewModel.getIs3DStateLiveData().observe(this, is3D -> {
            binding.ll3dMode.setSelected(is3D);
            binding.ll3d.setSelected(is3D);
        });
        viewModel.getUnOnClick().observe(this, this::isOften);
        viewModel.setConnectMac(mac);
    }

    private void upDateUI(boolean isConnected) {
        Log.i("DeviceModelImpl", "upDateUI: " + isConnected);
        setClickable(isConnected);
        if (isConnected) {
            if (binding.ll3dMode.getVisibility() == View.VISIBLE || binding.ll3d.getVisibility() == View.VISIBLE) {
                viewModel.get3DMode();
            }
            if (binding.llAnc.getVisibility() == View.VISIBLE) {
                viewModel.getAncMode();
            }
            viewModel.getBattery();
            viewModel.getWorkMode();
        } else {
            BatteryInfo batteryInfo = new BatteryInfo();
            viewModel.getBatteryLiveData().postValue(batteryInfo);
            viewModel.getWorkModeLiveData().postValue(WorkMode.NORMAL);
            viewModel.getIs3DStateLiveData().postValue(false);
            viewModel.getAncModeLiveData().postValue(AncMode.ERROR);
        }
    }

    @Override
    public void onBackPressed() {
        BluetoothManager.getInstance().disconnectRcsp(mac);
        super.onBackPressed();
    }

    private void initBatteryUI(BatteryInfo batteryInfo) {
        int leftBattery = batteryInfo.getLeftBattery();
        binding.tvBatteryLeft.setText(leftBattery == 0 ? "--" : leftBattery + "%");
        int rightBattery = batteryInfo.getRightBattery();
        binding.tvBatteryRight.setText(rightBattery == 0 ? "--" : rightBattery + "%");
        if (binding.llAll.getVisibility() == View.VISIBLE) {
            int caseBattery = batteryInfo.getCaseBattery();
            binding.tvAllBattery.setText(caseBattery == 0 ? "--" : caseBattery + "%");
            binding.skViewAll.setVisibility(caseBattery == 0 ? View.INVISIBLE : View.VISIBLE);
            binding.skViewAll.setProgressDrawable(getDrawableBg(caseBattery));
            binding.skViewAll.setProgress(caseBattery);
        }

        binding.skViewL.setVisibility(leftBattery == 0 ? View.INVISIBLE : View.VISIBLE);
        binding.skViewR.setVisibility(rightBattery == 0 ? View.INVISIBLE : View.VISIBLE);
        binding.skViewL.setProgressDrawable(getDrawableBg(leftBattery));
        binding.skViewR.setProgressDrawable(getDrawableBg(rightBattery));

        binding.skViewL.setProgress(leftBattery);
        binding.skViewR.setProgress(rightBattery);
    }

    private Drawable getDrawableBg(int battery) {
        return battery > 30 ?
                ContextCompat.getDrawable(this, R.drawable.progress_bar_background_1) :
                ContextCompat.getDrawable(this, R.drawable.progress_bar_background_2);
    }

    private void initANCUI(AncMode mode) {
        initState(mode == AncMode.ANC ? 1 : mode == AncMode.PASS ? 2 : mode == AncMode.OFF ? 3 : -1);
    }

    private void initState(int index) {
        binding.llAnc.setSelected(index == 1);
        binding.llTransparency.setSelected(index == 2);
        binding.llTurnOff.setSelected(index == 3);
    }

    private void isOften(boolean is) {
        if (is) {
            return;
        }
        Toast.makeText(DeviceEarInfoActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
    }

    public void setClickable(boolean clickable) {
        binding.clFactorySetting.setClickable(clickable);
        binding.clChangeName.setClickable(clickable);
        binding.llGameMode.setClickable(clickable);
        binding.llAnc.setClickable(clickable);
        binding.llTurnOff.setClickable(clickable);
        binding.llTransparency.setClickable(clickable);
        binding.ll3dMode.setClickable(clickable);
        binding.clEqSetting.setClickable(clickable);
        binding.clOtaUpdate.setClickable(clickable);
        binding.clKeySetting.setClickable(clickable);
    }

    @Override
    protected void initListener() {
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.clFactorySetting.setOnClickListener(v -> showResetDialog());
        binding.clChangeName.setOnClickListener(v -> showEditDialog());
        binding.llDeviceSearch.setOnClickListener(v -> startActivity(mac, DeviceEarFindActivity.class));
        binding.llGameMode.setOnClickListener(v -> viewModel.setWorkMode(binding.llGameMode.isSelected() ? WorkMode.NORMAL : WorkMode.GAME));
        binding.llAnc.setOnClickListener(v -> viewModel.setAncMode(AncMode.ANC));
        binding.llTurnOff.setOnClickListener(view -> viewModel.setAncMode(AncMode.OFF));
        binding.llTransparency.setOnClickListener(v -> viewModel.setAncMode(AncMode.PASS));
        binding.ll3d.setOnClickListener(view -> viewModel.set3DMode(!binding.ll3d.isSelected()));
        binding.ll3dMode.setOnClickListener(v -> viewModel.set3DMode(!binding.ll3dMode.isSelected()));
        binding.clMoreSetting.setOnClickListener(v -> DeviceMoreSettingActivity.start(null, mac, this));
        binding.clEqSetting.setOnClickListener(v -> startActivity(mac, DeviceEqSettingActivity.class));
        binding.clOtaUpdate.setOnClickListener(view -> startActivity(new Intent(this, DeviceOtaUpdateActivity.class)));
        binding.clKeySetting.setOnClickListener(view -> startActivity(new Intent(this, DeviceKeySettingActivity.class)));
    }

    private void showResetDialog() {
        msgDialogFragment.setTitle(getString(R.string.factory_settings));
        msgDialogFragment.setMsg(getString(R.string.reset_settings_context));
        msgDialogFragment.setConfirmText(getString(R.string.reset));
        msgDialogFragment.setOnDialogConfirmClick(() -> viewModel.getDeviceResetMode());
        msgDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void showEditDialog() {
        editDialogFragment.setLastEditText(TextUtils.isEmpty(binding.tvTitle.getText().toString()) ? "" : binding.tvTitle.getText().toString());
        editDialogFragment.setHintContext(getString(R.string.tv_null));
        editDialogFragment.setOnDialogClick(str -> viewModel.editDeviceName(str, isSuccess -> {
            if (isSuccess) {
                showEditNameSuccessTip();
                binding.tvTitle.setText(str);
            } else {
                showEditNameFailTip();
            }
        }));
        editDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void showEditNameSuccessTip() {
        msgDialogFragment.setTitle(getString(R.string.change_the_name));
        msgDialogFragment.setMsg(getString(R.string.edit_device_name_success));
        msgDialogFragment.setOnDialogConfirmClick(() -> {
            viewModel.rebootDevice();
            clearCustomSound(getIntent().getStringExtra(BundleConstant.MAC_KEY));
        });
        msgDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void clearCustomSound(String deviceAddress) {
        if (deviceAddress == null || deviceAddress.isEmpty()) {
            return;
        }
        spModel.putData(SpConstant.CUSTOM_SOUND_1 + "_" + deviceAddress, getString(R.string.custom_sound_1));
        spModel.putData(SpConstant.CUSTOM_SOUND_2 + "_" + deviceAddress, getString(R.string.custom_sound_2));
    }

    private void showEditNameFailTip() {
        msgDialogFragment.setTitle(getString(R.string.change_the_name));
        msgDialogFragment.setMsg(getString(R.string.edit_device_name_failed));
        msgDialogFragment.setShowConfirm(1);
        msgDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothManager.getInstance().connectRcsp(mac);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            viewModel.setConnectMac(mac);
        }, 300);
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
}
