package com.proove.smart.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.proove.ble.data.DevicePopInfo;
import com.proove.smart.databinding.DialogScanDeviceListBinding;


public class ScanDeviceListDialogFragment extends DialogFragment {

    private DevicePopInfo devicePopInfo;
    private Handler handler;
    private final DismissTask dismissTask = new DismissTask();
    private long dismissTime = 15000;
    private OnDialogClick onDialogClick;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogScanDeviceListBinding binding = DialogScanDeviceListBinding.inflate(requireActivity().getLayoutInflater());
        builder.setView(binding.getRoot());


        initData(binding);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        setCancelable(false);
        if (dismissTime != 0L) {
            handler.removeCallbacks(dismissTask);
            handler.postDelayed(dismissTask,dismissTime);
        }
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
        win.setBackgroundDrawable( new ColorDrawable(Color.TRANSPARENT));

        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics( dm );

        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.BOTTOM;
        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        params.width =  ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        win.setAttributes(params);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initData(DialogScanDeviceListBinding binding) {
        if (devicePopInfo != null) {

        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        handler.removeCallbacks(dismissTask);
    }

    public void setDismissTime(long dismissTime) {
        this.dismissTime = dismissTime;
    }

    public void setDevicePopInfo(DevicePopInfo devicePopInfo) {
        this.devicePopInfo = devicePopInfo;
    }

    public void setOnDialogClick(OnDialogClick onDialogClick) {
        this.onDialogClick = onDialogClick;
    }

    public interface OnDialogClick {
        void onClickConnect(DevicePopInfo devicePopInfo);
        void onCancel(DevicePopInfo devicePopInfo);
        void overTime(DevicePopInfo devicePopInfo);
    }

    private class DismissTask implements Runnable {

        @Override
        public void run() {
            try {
                dismiss();
            } catch (IllegalStateException e) {

            }
            if (onDialogClick != null) {
                onDialogClick.overTime(devicePopInfo);
            }
        }
    }
}
