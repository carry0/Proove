package com.proove.smart.ui;

import com.proove.smart.BuildConfig;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityTravelHistoryBinding;
import com.proove.smart.databinding.ActivityTravelHistoryDetailBinding;
import com.proove.smart.ui.base.BaseActivity;

public class TravelHistoryDetailActivity extends BaseActivity<ActivityTravelHistoryDetailBinding> {
    @Override
    protected ActivityTravelHistoryDetailBinding getViewBinding() {
        return ActivityTravelHistoryDetailBinding.inflate(getLayoutInflater());
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
