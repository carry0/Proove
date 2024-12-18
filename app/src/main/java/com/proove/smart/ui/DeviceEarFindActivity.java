package com.proove.smart.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.proove.ble.constant.BundleConstant;
import com.proove.ble.constant.DeviceFindState;
import com.proove.ble.data.DeviceFindInfo;
import com.proove.ble.data.DeviceInfo;
import com.proove.ble.data.DeviceMarker;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityDeviceEarInfoBinding;
import com.proove.smart.databinding.ActivityDeviceFindBinding;
import com.proove.smart.databinding.ViewMapMarkerBinding;
import com.proove.smart.manager.MediaPlayManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.ui.dialog.MapSelectDialogFragment;
import com.proove.smart.ui.dialog.MsgDialogFragment;
import com.proove.smart.util.FastClickUtil;
import com.proove.smart.util.MapUtil;
import com.proove.smart.vm.DeviceEarFindViewModel;
import com.proove.smart.vm.DeviceEarInfoViewModel;
import com.yscoco.lib.util.BluetoothUtil;
import com.yscoco.lib.util.ImageUtil;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.ThemeUtil;
import com.yscoco.lib.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class DeviceEarFindActivity extends BaseActivity<ActivityDeviceFindBinding> implements OnMapReadyCallback {
    private DeviceEarFindViewModel viewModel;
    private GoogleMap mMap;
    private final MsgDialogFragment msgDialogFragment = new MsgDialogFragment();
    private final MapSelectDialogFragment mapSelectDialogFragment = new MapSelectDialogFragment();
    private String mac;
    private boolean firstMove = true;

    @Override
    protected ActivityDeviceFindBinding getViewBinding() {
        return ActivityDeviceFindBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        viewModel.startLocation();
        mac = getIntent().getStringExtra(BundleConstant.MAC_KEY);
        getData(mac);
    }

    @Override
    protected void initView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(DeviceEarFindViewModel.class);
        viewModel.getDeviceFindInfoLiveData().observe(this, this::dealDeviceFindInfo);
        viewModel.getCurrentDeviceLiveData().observe(this, deviceInfo -> {
            Pair<Double, Double> deviceLocationPair = viewModel.getPositionPair(deviceInfo);
            if (deviceLocationPair != null) {
                moveMap(deviceLocationPair.first, deviceLocationPair.second);
            }
            binding.tvLocation.setText(viewModel.getDeviceLocationText()==null?"--":viewModel.getDeviceLocationText());
            binding.tvTime.setText(viewModel.getDeviceLastRecordTime());
            dealUserLocation();
        });
    }

    private void dealDeviceFindInfo(DeviceFindInfo deviceFindInfo) {
        if (deviceFindInfo.getLeftState() == DeviceFindState.RINGING
                || deviceFindInfo.getRightState() == DeviceFindState.RINGING) {
            updateRingUI(DeviceFindState.RINGING);
        } else {
            updateRingUI(DeviceFindState.NORMAL);
        }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateRingUI(DeviceFindState state) {
        binding.llPlayStop.setSelected(state == DeviceFindState.RINGING);
    }
    @Override
    protected void initListener() {
        binding.ivBack.setOnClickListener(v -> finish());
        binding.llPlayStop.setOnClickListener(v -> getPlay());
        binding.ivLocation.setOnClickListener(view -> getPosition());
        binding.llNavigation.setOnClickListener(v -> getNavigation());
    }
    private void getData(String mac) {
        if (!BluetoothUtil.isBluetoothAddress(mac)) {
            return;
        }
        viewModel.setCurrentDevice(mac);
        DeviceMarker deviceMarker = viewModel.getSingleDevice(mac);
        if (deviceMarker == null) {
            return;
        }
        List<DeviceMarker> deviceMarkerList = new ArrayList<>();
        deviceMarkerList.add(deviceMarker);
        AddDeviceMarker(deviceMarkerList, false);
    }
    private void getNavigation() {
        Pair<Double, Double> location = viewModel.getDeviceLocation();
        List<String> specificMapApplications = getSpecificMapApplications();
        if (specificMapApplications.isEmpty()) {
            ToastUtil.showToast(this, getString(R.string.no_map_app));
            return;
        }
        if (location == null) {
            return;
        }
        MapUtil.jumpMapApp(this, MapUtil.GOOGLE_MAP_PACKAGENAME, location.first, location.second, MapUtil.CoordType.BD09);
    }

    private void getPlay() {
        if (FastClickUtil.isFastClick()) {
            return;
        }
        if (binding.llPlayStop.isSelected()) {
            MediaPlayManager.getInstance().stop();
            binding.llPlayStop.setSelected(false);
        } else {
            if (!viewModel.isConnected(mac)) {
                ToastUtil.showToast(DeviceEarFindActivity.this, getString(R.string.not_connected_device));
            } else {
                showRingTipDialog();
            }
        }
    }

    private void getPosition() {
        Pair<Double, Double> pair = viewModel.getUserLocationLiveData().getValue();
        if (pair != null) {
            moveMap(pair.first, pair.second);
        }
        viewModel.startLocation();
    }


    private void moveMap(double latitude, double longitude) {
        if (latitude == 0.0d && longitude == 0.0d) {
            return;
        }
        LatLng latLng = new LatLng(latitude, longitude);
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    private void AddDeviceMarker(List<DeviceMarker> deviceMarkers, boolean isShowName) {
        if (deviceMarkers == null || deviceMarkers.isEmpty()) {
            return;
        }
        for (DeviceMarker deviceMarker : deviceMarkers) {
            if (deviceMarker == null) {
                continue;
            }
            ViewMapMarkerBinding mapMarkerBinding = ViewMapMarkerBinding.inflate(getLayoutInflater());
            mapMarkerBinding.ivDeviceImage.setImageResource(deviceMarker.getImageResId());
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromBitmap(ImageUtil.view2Bitmap(mapMarkerBinding.getRoot()));
            Bundle bundle = new Bundle();
            bundle.putString(BundleConstant.MAC_KEY, deviceMarker.getMac());
            LatLng latLng = new LatLng(deviceMarker.getLatitude(), deviceMarker.getLongitude());
            if (mMap != null) {
                mMap.addMarker(new MarkerOptions().icon(bitmap)
                        .position(latLng).draggable(false));
            }
        }
    }

    private void dealUserLocation() {
        Pair<Double, Double> locationPair = viewModel.getUserLocationLiveData().getValue();
        if (locationPair == null) {
            return;
        }
        LogUtil.info(TAG, "myLocation = " + locationPair);
        if (firstMove) {
            moveMap(locationPair.first, locationPair.second);
            firstMove = false;
        }
        Pair<Double, Double> deviceLocationPair = viewModel.getDeviceLocation();
        if (deviceLocationPair == null) {
            return;
        }
        float[] distanceResults = new float[1];
        Location.distanceBetween(
                locationPair.first, locationPair.second,
                deviceLocationPair.first, deviceLocationPair.second, distanceResults);
    }

    private void showRingTipDialog() {
        if (msgDialogFragment.isAdded() || !isVisible) {
            return;
        }
        msgDialogFragment.setTitle(getString(R.string.play_sound_hint_title));
        msgDialogFragment.setMsg(getString(R.string.play_sound_hint));
        msgDialogFragment.setConfirmText(getString(R.string.confirm));
        msgDialogFragment.setOnDialogConfirmClick(() -> {
            MediaPlayManager.getInstance().play(true);
            binding.llPlayStop.setSelected(true);
        });
        msgDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void showMapDialog(double latitude, double longitude) {
        if (mapSelectDialogFragment.isAdded() || !isVisible) {
            return;
        }
        mapSelectDialogFragment.setLatitudeAndLongitude(latitude, longitude);
        mapSelectDialogFragment.show(getSupportFragmentManager(), TAG);
    }
    private List<String> getSpecificMapApplications() {
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="));
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        List<String> specificMapApps = new ArrayList<>();
        for (ResolveInfo info : resolveInfos) {
            String packageName = info.activityInfo.packageName;
            if (packageName.contains("google")) {
                specificMapApps.add(packageName);
            }
        }
        return specificMapApps;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.setDeviceFindRing(DeviceFindState.NORMAL);
    }
}
