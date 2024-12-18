package com.proove.smart.ui;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.proove.ble.constant.Product;
import com.proove.ble.constant.protocol.DeviceSide;
import com.proove.ble.constant.protocol.UiAction;
import com.proove.ble.constant.protocol.UiFunction;
import com.proove.ble.data.UiInfo;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityEqSettingBinding;
import com.proove.smart.databinding.ActivityKeySettingBinding;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.ui.dialog.PickerDialogFragment;
import com.proove.smart.vm.DeviceEarInfoViewModel;
import com.proove.smart.vm.DeviceKeySettingViewModel;
import com.yscoco.lib.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class DeviceKeySettingActivity extends BaseActivity<ActivityKeySettingBinding> {
    private DeviceKeySettingViewModel viewModel;
    private UiAction uiAction = null;
    private final PickerDialogFragment pickerDialogFragment = new PickerDialogFragment();

    @Override
    protected ActivityKeySettingBinding getViewBinding() {
        return ActivityKeySettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(DeviceKeySettingViewModel.class);
        binding.setLifecycleOwner(this);

    }

    private void initViewLREar(DeviceSide side) {
        Product product = DeviceManager.getInstance().getCurrentProduct();
        if (product.getProductIconEarL()!=0){
            binding.ivEarLMagnify.setImageResource(product.getProductIconEarL());
            binding.ivEarRMagnify.setImageResource(product.getProductIconEarR());
            binding.ivEarL.setImageResource(product.getProductIconEarL());
            binding.ivEarR.setImageResource(product.getProductIconEarR());
        }
        binding.ivEarLMagnify.setVisibility(side == DeviceSide.LEFT ? View.VISIBLE : View.GONE);
        binding.ivEarRMagnify.setVisibility(side == DeviceSide.RIGHT ? View.VISIBLE : View.GONE);
        binding.ivEarL.setVisibility(side == DeviceSide.RIGHT ? View.VISIBLE : View.GONE);
        binding.ivEarR.setVisibility(side == DeviceSide.LEFT ? View.VISIBLE : View.GONE);
        binding.tvRight.setSelected(side == DeviceSide.RIGHT);
        binding.tvLeft.setSelected(side == DeviceSide.LEFT);

        viewModel.getUIInfo();
    }

    @Override
    protected void initData() {
        binding.setViewModel(viewModel);
        viewModel.getConnectStateLiveData().observe(this, aBoolean -> {
            if (!aBoolean) finish();
        });
        viewModel.setDeviceSide(DeviceSide.LEFT);
        initViewLREar(DeviceSide.LEFT);
        viewModel.getUiInfoLiveData().observe(this, this::updateKeySettingUI);
        viewModel.getCurrentDialog().observe(this, this::showSelectDialog);
    }

    @Override
    protected void initListener() {
        binding.tvLeft.setOnClickListener(v -> {
            if (viewModel.getDeviceSide() == DeviceSide.LEFT) {
                return;
            }
            initViewLREar(DeviceSide.LEFT);
            viewModel.setDeviceSide(DeviceSide.LEFT);
        });
        binding.tvRight.setOnClickListener(v -> {
            if (viewModel.getDeviceSide() == DeviceSide.RIGHT) {
                return;
            }
            initViewLREar(DeviceSide.RIGHT);
            viewModel.setDeviceSide(DeviceSide.RIGHT);
        });
    }

    private void updateKeySettingUI(UiInfo uiInfo) {
        DeviceSide deviceSide = viewModel.getDeviceSide();
        if (uiInfo == null || deviceSide == null) {
            return;
        }
        if (deviceSide == DeviceSide.LEFT) {
            binding.tvOne.setText(getUiFunctionString(uiInfo.getLeftOneClickFunction()));
            binding.tvTow.setText(getUiFunctionString(uiInfo.getLeftTwoClickFunction()));
            binding.tvThree.setText(getUiFunctionString(uiInfo.getLeftThreeClickFunction()));
            binding.tvLong.setText(getUiFunctionString(uiInfo.getLeftLongClickFunction()));
        } else if (deviceSide == DeviceSide.RIGHT) {
            binding.tvOne.setText(getUiFunctionString(uiInfo.getRightOneClickFunction()));
            binding.tvTow.setText(getUiFunctionString(uiInfo.getRightTwoClickFunction()));
            binding.tvThree.setText(getUiFunctionString(uiInfo.getRightThreeClickFunction()));
            binding.tvLong.setText(getUiFunctionString(uiInfo.getRightLongClickFunction()));
        }
    }

    public String getUiFunctionString(UiFunction action) {
        if (action == null) {
            return getString(R.string.none);
        }
        return switch (action) {
            case NONE -> getString(R.string.none);
            case VOICE_ASSISTANT -> getString(R.string.voice_assistant);
            case PREVIOUS_SONG -> getString(R.string.previous_song);
            case NEXT_SONG -> getString(R.string.next_song);
            case INCREASE_VOLUME -> getString(R.string.volume_1);
            case DECREASE_VOLUME -> getString(R.string.volume_2);
            case PLAY_OR_PAUSE -> getString(R.string.play_stop);
            case WORK_MODE -> getString(R.string.game_mode);
            case ANC -> getString(R.string.anc);
        };
    }

    private void showSelectDialog(Integer integer) {
        String title = "";
        switch (integer) {
            case 0 -> {
                uiAction = UiAction.CLICK;
                title = getString(R.string.one_touch);
            }
            case 1 -> {
                uiAction = UiAction.DOUBLE_CLICK;
                title = getString(R.string.two_touches);
            }
            case 2 -> {
                uiAction = UiAction.TRIPLE_HIT;
                title = getString(R.string.three_touches);
            }
            case 3 -> {
                uiAction = UiAction.LONG_PRESS;
                title = getString(R.string.press_and_hold);
            }
        }
        if (pickerDialogFragment.isAdded() || !isVisible || isDestroyed()) {
            return;
        }
        UiInfo uiInfo = viewModel.getUiInfoLiveData().getValue();
        DeviceSide deviceSide = viewModel.getDeviceSide();
        if (uiInfo == null || deviceSide == null) {
            return;
        }
        List<UiFunction> functionList = uiInfo.getUiFunctionList();
        if (functionList == null || functionList.isEmpty()) {
            return;
        }

        UiFunction oldUiFunction = UiFunction.NONE;
        if (deviceSide == DeviceSide.LEFT) {
            switch (uiAction) {
                case CLICK -> oldUiFunction = uiInfo.getLeftOneClickFunction();
                case DOUBLE_CLICK -> oldUiFunction = uiInfo.getLeftTwoClickFunction();
                case TRIPLE_HIT -> oldUiFunction = uiInfo.getLeftThreeClickFunction();
                case LONG_PRESS -> oldUiFunction = uiInfo.getLeftLongClickFunction();
            }
        } else if (deviceSide == DeviceSide.RIGHT) {
            switch (uiAction) {
                case CLICK -> oldUiFunction = uiInfo.getRightOneClickFunction();
                case DOUBLE_CLICK -> oldUiFunction = uiInfo.getRightTwoClickFunction();
                case TRIPLE_HIT -> oldUiFunction = uiInfo.getRightThreeClickFunction();
                case LONG_PRESS -> oldUiFunction = uiInfo.getRightLongClickFunction();
            }
        } else {
            return;
        }
        int preSelectPosition = functionList.indexOf(oldUiFunction);
        List<String> functionStrList = new ArrayList<>();
        for (UiFunction function : functionList) {
            functionStrList.add(getUiFunctionString(function));
        }
        pickerDialogFragment.setTitle(title);
        pickerDialogFragment.setData(functionStrList);
        pickerDialogFragment.setSelectedListener((position, item) -> {
            UiFunction newUiFunction = functionList.get(position);
            viewModel.setUi(deviceSide, uiAction, newUiFunction);
        });
        pickerDialogFragment.setDefaultPosition(preSelectPosition);
        pickerDialogFragment.show(getSupportFragmentManager(), TAG);
    }
}
