package com.proove.smart.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.proove.ble.constant.BundleConstant;
import com.proove.smart.databinding.ActivityPrivacyPolicyBinding;
import com.proove.smart.ui.base.BaseActivity;
import com.yscoco.lib.util.StringUtil;

public class WebActivity extends BaseActivity<ActivityPrivacyPolicyBinding> {

    @Override
    protected ActivityPrivacyPolicyBinding getViewBinding() {
        return ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        //binding.webView.setBackgroundColor(getColor(R.color.bg_color));
        // 设置 WebView 的 WebChromeClient 和 WebViewClient
        binding.webView.setWebChromeClient(new WebChromeClient());
        binding.webView.setWebViewClient(new WebViewClient());

// 启用 JavaScript
        binding.webView.getSettings().setJavaScriptEnabled(true);

// 启用 DOM 存储
        binding.webView.getSettings().setDomStorageEnabled(true);

// 允许文件访问
        binding.webView.getSettings().setAllowFileAccess(true);
        binding.webView.getSettings().setAllowFileAccessFromFileURLs(true);
        binding.webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

// 适配屏幕
        binding.webView.getSettings().setUseWideViewPort(true); // 使 WebView 能够适应不同宽度
        binding.webView.getSettings().setLoadWithOverviewMode(true); // 加载时，以适应内容区域显示

// 启用缩放功能，支持手势缩放
        binding.webView.getSettings().setSupportZoom(true);
        binding.webView.getSettings().setBuiltInZoomControls(true);
        binding.webView.getSettings().setDisplayZoomControls(false); // 隐藏缩放按钮（一般用于手势操作）

// 设置 WebView 的视图模式
        binding.webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

// 设置默认的字符编码
        binding.webView.getSettings().setDefaultTextEncodingName("utf-8");

// 设置 User-Agent (可选，适用于一些特殊页面适配需求)
        binding.webView.getSettings().setUserAgentString(System.getProperty("http.agent"));

    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        String title = intent.getStringExtra(BundleConstant.TITLE);
        if (!StringUtil.isNullOrEmpty(title)) {
            binding.tvTitle.setText(title);
        }
        String url = intent.getStringExtra(BundleConstant.URL);
        if (StringUtil.isNullOrEmpty(url)) {
            return;
        }
        if (TextUtils.equals(title,getString(com.proove.ble.R.string.about_us))){
            binding.clAbout.setVisibility(View.VISIBLE);
            binding.clPrivacy.setVisibility(View.GONE);
            binding.webViewAbout.loadUrl(url);
        }else {
            binding.clAbout.setVisibility(View.GONE);
            binding.clPrivacy.setVisibility(View.VISIBLE);
            binding.webView.loadUrl(url);
        }
    }

    @Override
    protected void initListener() {
        binding.ivBack.setOnClickListener(view -> finish());
    }
}
