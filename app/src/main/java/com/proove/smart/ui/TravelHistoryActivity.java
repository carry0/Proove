package com.proove.smart.ui;

import android.app.Activity;
import android.content.Intent;

import com.proove.smart.BuildConfig;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityPrivacyPolicyBinding;
import com.proove.smart.databinding.ActivityTravelHistoryBinding;
import com.proove.smart.ui.base.BaseActivity;

public class TravelHistoryActivity extends BaseActivity<ActivityTravelHistoryBinding> {
    public static void start(String name, String mac, Activity activity) {
        Intent intent = new Intent(activity, TravelHistoryActivity.class);
        intent.putExtra("DeviceName", name);
        intent.putExtra("DeviceMac", mac);
        activity.startActivity(intent);
    }
    @Override
    protected ActivityTravelHistoryBinding getViewBinding() {
        return ActivityTravelHistoryBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        binding.tvAppVersion.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        binding.ivBack.setOnClickListener(view -> finish());
    }
}
