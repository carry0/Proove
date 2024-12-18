package com.proove.smart.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;

import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DeviceListItem;
import com.proove.smart.databinding.DialogScanDeviceListBinding;
import com.proove.smart.ui.adapter.DeviceScanListAdapter;

import java.util.List;

public class ScanDeviceDialogFragment extends DialogFragment {
    private OnDialogConfirmClick onDialogConfirmClick;
    private List<DeviceListItem> listItems;
    DeviceScanListAdapter scanListAdapter = new DeviceScanListAdapter();
    private MutableLiveData<Boolean> dialogState = new MutableLiveData<>();

    public MutableLiveData<Boolean> getDialogState() {
        return dialogState;
    }

    public DeviceScanListAdapter getScanListAdapter() {
        return scanListAdapter;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        initCommonStyle(builder);
        Dialog dialog = builder.create();
        setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.0f);
        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        dialogState.setValue(false);
        super.onDismiss(dialog);
    }

    private void initCommonStyle(AlertDialog.Builder builder) {
        DialogScanDeviceListBinding binding = DialogScanDeviceListBinding.inflate(requireActivity().getLayoutInflater());
        builder.setView(binding.getRoot());
        dialogState.setValue(true);
        binding.rvScanList.setAdapter(scanListAdapter);
        scanListAdapter.submitList(listItems);

        scanListAdapter.setOnItemClickListener(deviceListItem -> {
            dismiss();
            if (onDialogConfirmClick == null) {
                return;
            }
            onDialogConfirmClick.onConfirm(deviceListItem);
        });
    }

    @Override
    public void dismiss() {
        dialogState.setValue(false);
        super.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }
        Window win = getDialog().getWindow();
        if (win == null) {
            return;
        }
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        win.setAttributes(params);
    }

    public void setListItems(List<DeviceListItem> listItems) {
        this.listItems = listItems;
    }

    public void setOnDialogConfirmClick(OnDialogConfirmClick onDialogConfirmClick) {
        this.onDialogConfirmClick = onDialogConfirmClick;
    }


    public interface OnDialogConfirmClick {
        void onConfirm(DeviceListItem deviceListItem);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onDialogConfirmClick = null;
        setCancelable(true);
    }
}
