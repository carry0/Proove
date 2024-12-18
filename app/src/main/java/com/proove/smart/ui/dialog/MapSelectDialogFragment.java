package com.proove.smart.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


import com.proove.smart.databinding.DialogChooseMapBinding;
import com.proove.smart.util.MapUtil;

import java.util.List;


public class MapSelectDialogFragment extends DialogFragment {
    private static final String TAG = "MapSelectDialogFragment";
    private double latitude;
    private double longitude;
    private String locationName;

    private List<String> listMap;

    public void setListMap(List<String> listMap) {
        this.listMap = listMap;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogChooseMapBinding binding = DialogChooseMapBinding.inflate(requireActivity().getLayoutInflater());
        for (String packageName : listMap) {
            if (packageName.contains("baidu")) {
                binding.tvBaiduMap.setVisibility( View.VISIBLE);
                binding.viewLine1.setVisibility(View.VISIBLE);
                continue;
            }
            if (packageName.contains("autonavi")) {
                binding.tvAmap.setVisibility(View.VISIBLE );
                binding.viewLine2.setVisibility( View.VISIBLE );
                continue;
            }
            if (packageName.contains("tencent")) {
                binding.tvTencentMap.setVisibility( View.VISIBLE);
                binding.viewLine3.setVisibility( View.VISIBLE );
                continue;
            }
            if (packageName.contains("google")) {
                binding.tvGoogleMap.setVisibility( View.VISIBLE);
                binding.viewLine4.setVisibility(View.VISIBLE);
            }

        }
        binding.tvCancel.setOnClickListener(view -> dismiss());
        binding.tvAmap.setOnClickListener(view -> {
            MapUtil.jumpMapApp(getContext(), MapUtil.GAODEMAP_PACKAGENAME, longitude, latitude, MapUtil.CoordType.BD09);
            dismiss();
        });
        binding.tvBaiduMap.setOnClickListener(view -> {
            MapUtil.jumpMapApp(getContext(), MapUtil.BAIDUMAP_PACKAGENAME, longitude, latitude, MapUtil.CoordType.BD09);
            dismiss();
        });
        binding.tvTencentMap.setOnClickListener(view -> {
            MapUtil.jumpMapApp(getContext(), MapUtil.QQMAP_PACKAGENAME, longitude, latitude, MapUtil.CoordType.BD09);
            dismiss();
        });
        binding.tvGoogleMap.setOnClickListener(view -> {
            MapUtil.jumpMapApp(getContext(), MapUtil.GOOGLE_MAP_PACKAGENAME, longitude, latitude, MapUtil.CoordType.BD09);
            dismiss();
        });
        binding.tvLocationName.setText(locationName);
        builder.setView(binding.getRoot());
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setDimAmount(0.4f);
        return dialog;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }
        // 设置宽度为屏宽, 靠近屏幕底部。
        Window win = getDialog().getWindow();
        if (win == null) {
            return;
        }
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.BOTTOM;
        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        win.setAttributes(params);
    }

    public void setLatitudeAndLongitude(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
