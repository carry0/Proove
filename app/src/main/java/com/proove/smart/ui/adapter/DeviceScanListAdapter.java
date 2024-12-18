package com.proove.smart.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DeviceListItem;
import com.proove.smart.databinding.ItemScanDeviceBinding;


public class DeviceScanListAdapter extends ListAdapter<DeviceListItem, RecyclerView.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public DeviceScanListAdapter() {
        super(new DiffUtil.ItemCallback<DeviceListItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull DeviceListItem oldItem, @NonNull DeviceListItem newItem) {
                if (oldItem.getMac() == null) {
                    return false;
                }
                return oldItem.getMac().equals(newItem.getMac());
            }

            @Override
            public boolean areContentsTheSame(@NonNull DeviceListItem oldItem, @NonNull DeviceListItem newItem) {
                return oldItem.isConnected() == newItem.isConnected()
                        && oldItem.getDeviceName().equals(newItem.getDeviceName());
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScanDeviceBinding binding = ItemScanDeviceBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DeviceItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DeviceItemViewHolder deviceItemViewHolder = (DeviceItemViewHolder) holder;
        DeviceListItem deviceListItem = getItem(position);
        ItemScanDeviceBinding binding = deviceItemViewHolder.binding;
        binding.tvScanDeviceName.setText(deviceListItem.getDeviceName());
        deviceItemViewHolder.setOnLongClickListener(view -> {
            if (onItemLongClickListener == null) {
                return false;
            }
            onItemLongClickListener.onLongClick(deviceListItem);
            return true;
        });
        deviceItemViewHolder.setOnClickListener(view -> {
            if (onItemClickListener == null) {
                return;
            }
            onItemClickListener.onClick(deviceListItem);
        });

    }

    private static class DeviceItemViewHolder extends RecyclerView.ViewHolder {
        ItemScanDeviceBinding binding;

        public DeviceItemViewHolder(@NonNull ItemScanDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setOnClickListener(View.OnClickListener listener) {
            binding.getRoot().setOnClickListener(listener);
        }

        public void setOnLongClickListener(View.OnLongClickListener listener) {
            binding.getRoot().setOnLongClickListener(listener);
        }
    }

    public interface OnItemClickListener {
        void onClick(DeviceListItem deviceListItem);
    }

    public interface OnItemLongClickListener {
        void onLongClick(DeviceListItem deviceListItem);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }
}
