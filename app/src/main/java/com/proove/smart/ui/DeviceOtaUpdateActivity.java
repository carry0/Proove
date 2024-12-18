package com.proove.smart.ui;

import android.app.Activity;
import android.content.Intent;

import com.proove.smart.databinding.ActivityEqSettingBinding;
import com.proove.smart.databinding.ActivityOtaUpdateBinding;
import com.proove.smart.ui.base.BaseActivity;

public class DeviceOtaUpdateActivity extends BaseActivity<ActivityOtaUpdateBinding> {

    public static void start(String name, String mac, Activity activity) {
        Intent intent = new Intent(activity, DeviceOtaUpdateActivity.class);
        intent.putExtra("DeviceName", name);
        intent.putExtra("DeviceMac", mac);
        activity.startActivity(intent);
    }
    @Override
    protected ActivityOtaUpdateBinding getViewBinding() {
        return ActivityOtaUpdateBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        binding.ivBack.setOnClickListener(v -> finish());
    }
}
