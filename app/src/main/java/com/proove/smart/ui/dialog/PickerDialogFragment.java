package com.proove.smart.ui.dialog;


import static com.github.gzuliyujiang.wheelview.annotation.CurtainCorner.ALL;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.github.gzuliyujiang.wheelpicker.widget.BaseWheelLayout;
import com.github.gzuliyujiang.wheelview.annotation.CurtainCorner;
import com.proove.smart.R;
import com.proove.smart.databinding.DialogPickerBinding;
import com.yscoco.lib.util.DisplayUtil;

import java.util.List;

public class PickerDialogFragment extends DialogFragment {

    private List<?> data;
    private OnSelectedListener listener;
    private int defaultPosition;
    private String title = "xxx";

    public interface OnSelectedListener {
        void onSelected(int position, String str);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogPickerBinding binding = DialogPickerBinding.inflate(requireActivity().getLayoutInflater());
        builder.setView(binding.getRoot());
        binding.tvTitle.setText(title);
        binding.ivBack.setOnClickListener(v -> dismiss());
        initPickerStyle(binding.wheelOption);
        if (data != null) {
            binding.wheelOption.setData(data);
        }
        binding.wheelOption.setDefaultPosition(defaultPosition);

        binding.wheelOption.setOnOptionSelectedListener((position, item) -> {
            listener.onSelected(position, String.valueOf(item));
            dismiss();
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.5f);

        }
        return dialog;
    }

    private void initPickerStyle(BaseWheelLayout wheelLayout) {
        wheelLayout.setIndicatorEnabled(true);
        wheelLayout.setAtmosphericEnabled(true);
        wheelLayout.setIndicatorColor(getContext().getColor(R.color.v_scan_line_c));
        wheelLayout.setVisibleItemCount(data.size());
        wheelLayout.setSelectedTextBold(true);
        wheelLayout.setSelectedTextColor(getContext().getColor(R.color.tv_color));
        wheelLayout.setTextColor(getContext().getColor(R.color.tv_hint_color));
        wheelLayout.setSelectedTextSize(DisplayUtil.spToPx(getContext(), 20.0f));
        wheelLayout.setTextSize(DisplayUtil.spToPx(getContext(), 18.0f));
    }

    public void setDefaultPosition(int defaultPosition) {
        this.defaultPosition = defaultPosition;
    }

    public void setData(List<String> data) {
        if (data == null) {
            return;
        }
        this.data = data;
    }

    public void setSelectedListener(OnSelectedListener listener) {
        if (listener == null) {
            return;
        }
        this.listener = listener;
    }
}
