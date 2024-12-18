package com.proove.smart.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationManager;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.hjq.permissions.XXPermissions;
import com.proove.ble.constant.Constant;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivitySplashBinding;
import com.proove.smart.manager.BleManager;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.util.SpModel;
import com.tencent.bugly.crashreport.CrashReport;
import com.yscoco.lib.constant.SpConstant;
import com.yscoco.lib.util.ActivityUtil;
import com.yscoco.lib.util.SpUtil;
import com.yscoco.lib.util.ThemeUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<ActivitySplashBinding> {

    private static final long DELAY_TIME = 1000L;
    private final Handler handler = new Handler();

    private final Runnable jumpTask = () -> {
        jumpHomePage();
    };

    @Override
    protected ActivitySplashBinding getViewBinding() {
        return ActivitySplashBinding.inflate(getLayoutInflater());
    }


    @Override
    protected void initView() {
        ThemeUtil.setStatusBarTransparent(getWindow());
    }


    @Override
    protected void initData() {
        loadSetting();
        initSDK();
        handler.postDelayed(jumpTask, DELAY_TIME);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(jumpTask);
        finish();
    }

    private void jumpHomePage() {
        String[] needGrantPermission = getNeedGrantPermission();
        if (needGrantPermission.length > 0) {
            showPermissionDialog(
                    () -> XXPermissions.with(SplashActivity.this)
                            .permission(needGrantPermission)
                            .request((permissions, allGranted) -> {
                                ActivityUtil.jump(this, HomeActivity.class);
                                finish();
                            }),
                    this::finish);
            return;
        }
        ActivityUtil.jump(this, HomeActivity.class);
        finish();
    }

    private void loadSetting() {
        ThemeUtil.changeNightMode((int)spModel.getData(SpConstant.CURRENT_THEME_KEY, 1));
    }


    private void initSDK() {
        DeviceManager.getInstance().init();
        CrashReport.initCrashReport(this, Constant.BUGLY_SDK_APIKEY, false);
    }
}
