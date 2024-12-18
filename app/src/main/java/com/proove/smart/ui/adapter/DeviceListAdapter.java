package com.proove.smart.ui.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.proove.ble.data.DeviceListItem;
import com.proove.smart.R;
import com.proove.smart.databinding.ItemDeviceBinding;


public class DeviceListAdapter extends ListAdapter<DeviceListItem, RecyclerView.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public DeviceListAdapter() {
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
        ItemDeviceBinding binding = ItemDeviceBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DeviceItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DeviceItemViewHolder deviceItemViewHolder = (DeviceItemViewHolder) holder;
        DeviceListItem deviceListItem = getItem(position);
        ItemDeviceBinding binding = deviceItemViewHolder.binding;
        if (deviceListItem.equalsEarDevice()){
            binding.tvTitleHint.setText(binding.getRoot().getContext().getString(R.string.headphones));
        }else if (deviceListItem.equalsDevice()){
            binding.tvTitleHint.setText(binding.getRoot().getContext().getString(R.string.electric_scooters));
        }else {
            binding.tvTitleHint.setText(binding.getRoot().getContext().getString(R.string.smartwatch));
        }
        binding.tvHomeDeviceName.setText(deviceListItem.getDeviceName());
        binding.ivState.setSelected(deviceListItem.deviceInfo.isConnected());
        binding.imageView.setImageResource(deviceListItem.getProductImageResId());
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
        ItemDeviceBinding binding;

        public DeviceItemViewHolder(@NonNull ItemDeviceBinding binding) {
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
