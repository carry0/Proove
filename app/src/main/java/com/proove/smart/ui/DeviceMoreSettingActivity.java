package com.proove.smart.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.hjq.permissions.XXPermissions;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.proove.ble.constant.BundleConstant;
import com.proove.ble.constant.Constant;
import com.proove.ble.constant.DevicePopState;
import com.proove.ble.constant.SpConstant;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityMoreSettingBinding;
import com.proove.smart.manager.ActivityManager;
import com.proove.smart.manager.BleManager;
import com.proove.smart.manager.MediaPlayManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.ui.dialog.MsgDialogFragment;
import com.proove.smart.ui.dialog.PickerDialogFragment;
import com.proove.smart.vm.DeviceEarInfoViewModel;
import com.proove.smart.vm.DeviceListViewModel;
import com.proove.smart.vm.DevicePopViewModel;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.LocationUtil;
import com.yscoco.lib.util.MultiLanguageUtils;
import com.yscoco.lib.util.SpUtil;
import com.yscoco.lib.util.ToastUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DeviceMoreSettingActivity extends BaseActivity<ActivityMoreSettingBinding> {
    private final PickerDialogFragment pickerDialogFragment = new PickerDialogFragment();
    private final MsgDialogFragment msgDialogFragment = new MsgDialogFragment();
    private List<String> languages;
    private List<String> themes;
    String stringExtra;
    private DeviceEarInfoViewModel viewModel;

    public static void start(String name, String mac, Activity activity) {
        Intent intent = new Intent(activity, DeviceMoreSettingActivity.class);
        intent.putExtra("DeviceName", name);
        intent.putExtra(BundleConstant.MAC_KEY, mac);
        activity.startActivityForResult(intent, 1001);
    }

    @Override
    protected ActivityMoreSettingBinding getViewBinding() {
        return ActivityMoreSettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        stringExtra = getIntent().getStringExtra(BundleConstant.MAC_KEY);
        viewModel = new ViewModelProvider(this).get(DeviceEarInfoViewModel.class);
    }

    @Override
    protected void initData() {
        languages = List.of(getString(R.string.uk), getString(R.string.english));
        themes = List.of(getString(R.string.dark_theme), getString(R.string.bright_theme));
        binding.swPopUp.setChecked(DeviceListViewModel.isDevicePopupOn());
    }

    private int getCurrentTheme() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    protected void initListener() {
        binding.clRemoveDevice.setVisibility(getIntent().getStringExtra("DeviceName") == null ? View.VISIBLE : View.GONE);
        binding.ivBack.setOnClickListener(v -> finish());
        binding.swPopUp.setOnCheckedChangeListener(this::setPopUp);
        binding.clLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.clDarkTheme.setOnClickListener(v -> showDarkThemeDialog());
        binding.clAboutUs.setOnClickListener(v -> jumpAboutAs());
        binding.clPrivacyPolicy.setOnClickListener(v -> jumpPrivacyPolicy());
        binding.clUserProtocol.setOnClickListener(v -> jumpUserProtocol());
        binding.clRemoveDevice.setOnClickListener(v -> showRemoveDeviceDialog());

    }

    private void setPopUp(@NonNull CompoundButton buttonView, boolean isChecked) {
        if (!buttonView.isPressed()) {
            return;
        }
        if (!BluetoothUtil.isBluetoothEnable()) {
            showOpenBluetoothTip();
            buttonView.setChecked(false);
            return;
        }
        if (!LocationUtil.isLocationOpen(this)) {
            showOpenLocationTip();
            buttonView.setChecked(false);
            return;
        }
        String[] needGrantPermission = getNeedGrantPermission();
        if (needGrantPermission.length > 0) {
            showPermissionDialog(
                    () -> XXPermissions.with(DeviceMoreSettingActivity.this)
                            .permission(needGrantPermission)
                            .request((permissions, allGranted) -> {
                                if (allGranted) {
                                    DeviceListViewModel.setDevicePopState(DevicePopState.ON);
                                    buttonView.setChecked(true);
                                }
                            }),
                    () -> buttonView.setChecked(false));
            buttonView.setChecked(false);
            return;
        }
        DeviceListViewModel.setDevicePopState(isChecked ? DevicePopState.ON : DevicePopState.OFF);
    }

    private void showLanguageDialog() {
        pickerDialogFragment.setTitle(getString(R.string.language));
        pickerDialogFragment.setData(languages);
        Locale locale = MultiLanguageUtils.getLanguageSetting();
        int position;
        if (locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
            position = 1;
        } else if (locale.getLanguage().equals("uk")) {
            position = 0;
        } else {
            position = 1;
        }
        pickerDialogFragment.setDefaultPosition(position);
        pickerDialogFragment.setSelectedListener((p, str) -> {
            if (p == position) {
                return;
            }
            dealLanguageSwitch(str);
        });
        pickerDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void dealLanguageSwitch(String item) {
        if (item.equals(getString(R.string.uk))) {
            MultiLanguageUtils
                    .changeLanguage(this, "uk", Locale.UK.getCountry());
        }
        if (item.equals(getString(R.string.english))) {
            MultiLanguageUtils
                    .changeLanguage(this, Locale.ENGLISH.getLanguage(), Locale.ENGLISH.getCountry());
        }
        ActivityManager.getInstance().recreate();
    }

    private void showDarkThemeDialog() {
        pickerDialogFragment.setTitle(getString(R.string.dark_theme));
        pickerDialogFragment.setData(themes);
        pickerDialogFragment.setDefaultPosition(getCurrentTheme());
        pickerDialogFragment.setSelectedListener((position, str) -> {
            if (position == getCurrentTheme()) {
                return;
            }
            dealThemeSwitch(str);
        });
        pickerDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void dealThemeSwitch(String item) {
        if (item.equals(getString(R.string.bright_theme))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            spModel.putData(SpConstant.CURRENT_THEME_KEY, 1);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            spModel.putData(SpConstant.CURRENT_THEME_KEY, 2);
        }
    }

    private void showRemoveDeviceDialog() {
        msgDialogFragment.setTitle(getString(R.string.remove_device) + "?");
        msgDialogFragment.setMsg(getString(R.string.are_you_sure_you_want_to_remove_the_device_from_the_proove_app));
        msgDialogFragment.setConfirmText(getString(R.string.remove));
        msgDialogFragment.setCancelText(getString(R.string.cancel));
        msgDialogFragment.setOnDialogConfirmClick(() -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                BleManager.getInstance().disconnectGatt();
                viewModel.deleteDevice(stringExtra);
                spModel.putData(SpConstant.CUSTOM_SOUND_1 + "_" + stringExtra, getString(R.string.custom_sound_1));
                spModel.putData(SpConstant.CUSTOM_SOUND_2 + "_" + stringExtra, getString(R.string.custom_sound_2));
                Intent resultIntent = new Intent();
                resultIntent.putExtra("key", "deleteDevice");
                setResult(RESULT_OK, resultIntent);
                finish();
            }, 500);
        });
        msgDialogFragment.show(getSupportFragmentManager(), TAG);
    }


}
