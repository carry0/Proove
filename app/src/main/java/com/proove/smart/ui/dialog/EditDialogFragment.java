package com.proove.smart.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.proove.smart.R;
import com.proove.smart.databinding.DialogEqChangeNameBinding;
import com.yscoco.lib.util.StringUtil;

public class EditDialogFragment extends DialogFragment {
    private OnDialogClick onDialogClick;
    private String lastEditText;
    private String title;
    private String hintText;
    private String hintContext=  "";
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogEqChangeNameBinding binding = DialogEqChangeNameBinding
                .inflate(requireActivity().getLayoutInflater());
        binding.ivBack.setOnClickListener(v -> dismiss());
        if (!StringUtil.isNullOrEmpty(lastEditText)) {
            binding.editText.setText(lastEditText);
        }
        if (!StringUtil.isNullOrEmpty(title)) {
            binding.tvTitle.setText(title);
        }
        if (!StringUtil.isNullOrEmpty(hintText)) {
            binding.editText.setHint(hintText);
        }
        builder.setView(binding.getRoot());

        binding.butSave.setOnClickListener(view -> {
            if (binding.editText.getText().toString().isEmpty()){
                Toast.makeText(getContext(), hintContext, Toast.LENGTH_SHORT).show();
                return;
            }
            dismiss();
            if (onDialogClick == null) {
                return;
            }
            onDialogClick.onConfirm(binding.editText.getText().toString());
        });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.5f);
        return dialog;
    }

    public void setHintContext(String hintContext) {
        this.hintContext = hintContext;
    }

    public void setOnDialogClick(OnDialogClick onDialogClick) {
        this.onDialogClick = onDialogClick;
    }

    public void setLastEditText(String lastEditText) {
        this.lastEditText = lastEditText;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public interface OnDialogClick {
        void onConfirm(String text);
    }
}
