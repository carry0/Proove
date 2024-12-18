package com.proove.smart.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.proove.smart.R;
import com.proove.smart.databinding.DialogMsgBinding;
import com.yscoco.lib.util.StringUtil;

public class MsgDialogFragment extends DialogFragment {
    private String title = "";
    private String msg = "";
    private String confirmText = "";
    private String cancelText = "";
    private int showConfirm = 0;
    private int showBack = 0;
    public OnDialogCancelClick onDialogCancelClick;
    public OnDialogConfirmClick onDialogConfirmClick;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        initCommonStyle(builder);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.5f);
        return dialog;
    }

    public void setShowBack(int showBack) {
        this.showBack = showBack;
    }

    private void initCommonStyle(AlertDialog.Builder builder) {
        DialogMsgBinding binding = DialogMsgBinding.inflate(requireActivity().getLayoutInflater());
        builder.setView(binding.getRoot());
        binding.tvMsgTitle.setText(title);
        binding.ivBack.setOnClickListener(v -> dismiss());
        binding.tvContent.setText(msg);
        binding.ivBack.setVisibility(showBack==0?View.VISIBLE:View.GONE);
        binding.tvOk.setVisibility(showConfirm==0?View.VISIBLE:View.GONE);
        binding.tvCancel.setVisibility(showConfirm==0?View.VISIBLE:View.GONE);

        if (!StringUtil.isNullOrEmpty(confirmText)) {
            binding.tvOk.setText(confirmText);
        }
        if (!StringUtil.isNullOrEmpty(cancelText)) {
            binding.tvCancel.setText(cancelText);
        }
        binding.tvOk.setOnClickListener(view -> {
            dismiss();
            if (onDialogConfirmClick == null) {
                return;
            }
            onDialogConfirmClick.onConfirm();
        });
        binding.tvCancel.setOnClickListener(view -> {
            dismiss();
            if (onDialogCancelClick == null) {
                return;
            }
            onDialogCancelClick.onCancel();
        });
    }

    public void setShowConfirm(int showConfirm) {
        this.showConfirm = showConfirm;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText;
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }

    public void setOnDialogCancelClick(OnDialogCancelClick onDialogCancelClick) {
        this.onDialogCancelClick = onDialogCancelClick;
    }

    public void setOnDialogConfirmClick(OnDialogConfirmClick onDialogConfirmClick) {
        this.onDialogConfirmClick = onDialogConfirmClick;
    }

    public interface OnDialogCancelClick {
        void onCancel();
    }

    public interface OnDialogConfirmClick {
        void onConfirm();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onDialogConfirmClick = null;
        onDialogCancelClick = null;
        title = StringUtil.EMPTY;
        msg = StringUtil.EMPTY;
        setCancelable(true);
    }
}
