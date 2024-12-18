package com.proove.smart.ui.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewbinding.ViewBinding;

import com.hjq.permissions.XXPermissions;
import com.jieli.bluetooth.bean.BluetoothOption;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.proove.ble.constant.BundleConstant;
import com.proove.ble.constant.Constant;
import com.proove.ble.constant.SpConstant;
import com.proove.smart.R;
import com.proove.smart.manager.BluetoothManager;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.ui.DeviceMoreSettingActivity;
import com.proove.smart.ui.WebActivity;
import com.proove.smart.ui.dialog.MsgDialogFragment;
import com.proove.smart.util.SpModel;
import com.yscoco.lib.util.LocationUtil;
import com.yscoco.lib.util.MultiLanguageUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {
    protected final String TAG = this.getClass().getSimpleName();
    protected T binding;
    protected SpModel spModel;
    protected boolean isVisible;
    private final MsgDialogFragment msgDialogFragment = new MsgDialogFragment();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MultiLanguageUtils.attachBaseContext(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupStatusBar();
        binding = getViewBinding();
        setContentView(binding.getRoot());
        spModel = SpModel.getInstance(BaseActivity.this, Constant.APP_TABLE_NAME);
        getNeedGrantPermission();
        initView();
        initData();
        initListener();
    }
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            // 设置状态栏颜色
            window.setStatusBarColor(getResources().getColor(R.color.bg_home_color));
            // 设置状态栏图标为深色
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    protected void startActivity( String mac,Class clazz) {
        Intent intent = new Intent(this, clazz);
        intent.putExtra(BundleConstant.MAC_KEY, mac);
        startActivity(intent);
    }
    protected void startActivity( String mac,String deviceName,Class clazz) {
        Intent intent = new Intent(this, clazz);
        intent.putExtra(BundleConstant.MAC_KEY, mac);
        intent.putExtra(BundleConstant.TITLE,deviceName);
        startActivity(intent);
    }
    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    protected void jumpPrivacyPolicy() {
        String url;
        Locale locale = MultiLanguageUtils.getAppLocale(this);
        if (Locale.CHINESE.getLanguage().equals(locale.getLanguage())) {
            url = Constant.PRIVACY_POLICY_ZH;
        } else {
            url = Constant.PRIVACY_POLICY_EN;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    protected void jumpAboutAs() {
        String url;
        Locale locale = MultiLanguageUtils.getAppLocale(this);
        if (Locale.CHINESE.getLanguage().equals(locale.getLanguage())) {
            url = Constant.ABOUT_US_ZH;
        } else {
            url = Constant.ABOUT_US_EN;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    protected void jumpUserProtocol() {
        String url;
        Locale locale = MultiLanguageUtils.getAppLocale(this);
        if (Locale.CHINESE.getLanguage().equals(locale.getLanguage())) {
            url = Constant.USER_AGREEMENT_ZH;
        } else {
            url = Constant.USER_AGREEMENT_EN;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    protected void showOpenBluetoothTip() {
        if (msgDialogFragment.isAdded()) {
            return;
        }
        msgDialogFragment.setTitle(getString(R.string.bluetooth_not_turned_on));
        msgDialogFragment.setMsg(getString(R.string.whether_to_jump_to_the_bluetooth_settings_screen));
        msgDialogFragment.setConfirmText(getString(R.string.confirm));
        msgDialogFragment.setOnDialogConfirmClick(() -> startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)));
        msgDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    protected void showOpenLocationTip() {
        if (msgDialogFragment.isAdded()) {
            return;
        }
        msgDialogFragment.setTitle(getString(R.string.the_location_service_is_not_enabled));
        msgDialogFragment.setMsg(getString(R.string.whether_to_switch_to_the_location_service_setting_page));
        msgDialogFragment.setConfirmText(getString(R.string.confirm));
        msgDialogFragment.setOnDialogConfirmClick(() -> LocationUtil.jumpLocationSetting(BaseActivity.this));
        msgDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    protected void showPermissionDialog(MsgDialogFragment.OnDialogConfirmClick listener, MsgDialogFragment.OnDialogCancelClick cancel) {
        if (msgDialogFragment.isAdded()) {
            return;
        }
        msgDialogFragment.setTitle(getString(R.string.location));
        msgDialogFragment.setMsg(getString(R.string.location_context));
        msgDialogFragment.setConfirmText(getString(R.string.confirm));
        msgDialogFragment.setShowBack(1);
        msgDialogFragment.setOnDialogConfirmClick(listener);
        msgDialogFragment.setOnDialogCancelClick(cancel);
        msgDialogFragment.show(getSupportFragmentManager(), TAG);
    }
    @NonNull
    protected String[] getNeedGrantPermission() {
        Set<String> needGrantPermission = new HashSet<>();
        String[] allPermission;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            allPermission = Constant.PERMISSIONS_V31;
        } else {
            allPermission = Constant.PERMISSIONS;
        }
        for (String permission : allPermission) {
            if (!XXPermissions.isGranted(this, permission)) {
                needGrantPermission.add(permission);
            }
        }
        return needGrantPermission.toArray(new String[0]);
    }

    protected abstract T getViewBinding();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initListener();
}
