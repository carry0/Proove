package com.proove.smart.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.proove.smart.databinding.DialogLoadingBinding;
import com.yscoco.lib.util.AnimUtil;

public class LoadingDialogFragment extends DialogFragment {

    private Handler handler;
    private final DismissTask dismissTask = new DismissTask();
    private long dismissTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogLoadingBinding binding = DialogLoadingBinding.inflate(requireActivity().getLayoutInflater());
        AnimUtil.startRotateAnimation(binding.ivLoading, 1);
        builder.setView(binding.getRoot());
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setDimAmount(0.5f);
        if (dismissTime != 0L) {
            handler.removeCallbacks(dismissTask);
            handler.postDelayed(dismissTask,dismissTime);
        }
        return dialog;
    }

    public void setDismissTime(long dismissTime) {
        this.dismissTime = dismissTime;
    }

    private class DismissTask implements Runnable {

        @Override
        public void run() {
            dismiss();
        }
    }
}
