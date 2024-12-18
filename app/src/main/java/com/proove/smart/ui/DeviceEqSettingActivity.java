package com.proove.smart.ui;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.databinding.BindingAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.proove.ble.constant.BundleConstant;
import com.proove.ble.constant.CommonEqInfo;
import com.proove.ble.constant.EqConstant;
import com.proove.ble.constant.EqPreset;
import com.proove.ble.constant.Product;
import com.proove.ble.constant.SpConstant;
import com.proove.smart.R;
import com.proove.smart.databinding.ActivityEqSettingBinding;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.ui.base.BaseActivity;
import com.proove.smart.ui.dialog.EditDialogFragment;
import com.proove.smart.ui.dialog.PickerDialogFragment;
import com.proove.smart.vm.DeviceEqSettingViewModel;
import com.yscoco.lib.util.LogUtil;
import com.yscoco.lib.util.SpUtil;
import com.yscoco.lib.util.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceEqSettingActivity extends BaseActivity<ActivityEqSettingBinding> {
    private DeviceEqSettingViewModel viewModel;

    private List<SeekBar> sbAdjust, sbDefault;
    private final EditDialogFragment editDialogFragment = new EditDialogFragment();

    private final PickerDialogFragment pickerDialogFragment = new PickerDialogFragment();

    private List<String> customSounds;

    private int position = 0;

    private final SeekBar.OnSeekBarChangeListener eqSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            saveAndSetCustomEq();
        }
    };

    private String deviceAddress;
    private EqPreset eqPreset;

    @Override
    protected ActivityEqSettingBinding getViewBinding() {
        return ActivityEqSettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(DeviceEqSettingViewModel.class);
        binding.setLifecycleOwner(this);

        deviceAddress = getIntent().getStringExtra(BundleConstant.MAC_KEY);
        if (deviceAddress == null) {
            finish();
            return;
        }

        position = (int) spModel.getData(SpConstant.CUSTOM_SOUND + "_" + deviceAddress, 0);
        String sound1 = (String) spModel.getData(SpConstant.CUSTOM_SOUND_1 + "_" + deviceAddress, getString(R.string.custom_sound_1));
        String sound2 = (String) spModel.getData(SpConstant.CUSTOM_SOUND_2 + "_" + deviceAddress, getString(R.string.custom_sound_2));
        sbAdjust = List.of(
                binding.icEqAdjust.sbEq1,
                binding.icEqAdjust.sbEq2,
                binding.icEqAdjust.sbEq3,
                binding.icEqAdjust.sbEq4,
                binding.icEqAdjust.sbEq5,
                binding.icEqAdjust.sbEq6,
                binding.icEqAdjust.sbEq7,
                binding.icEqAdjust.sbEq8,
                binding.icEqAdjust.sbEq9,
                binding.icEqAdjust.sbEq10);

        sbDefault = List.of(
                binding.icEqDefault.sbEq1,
                binding.icEqDefault.sbEq2,
                binding.icEqDefault.sbEq3,
                binding.icEqDefault.sbEq4,
                binding.icEqDefault.sbEq5,
                binding.icEqDefault.sbEq6,
                binding.icEqDefault.sbEq7,
                binding.icEqDefault.sbEq8,
                binding.icEqDefault.sbEq9,
                binding.icEqDefault.sbEq10);

        customSounds = new ArrayList<>();
        customSounds.add(sound1);
        customSounds.add(sound2);
        binding.tvEqCustom.setText(customSounds.get(position));


        Product product = DeviceManager.getInstance().getCurrentProduct();
        if (product == null) {
            return;
        }
        if (product.getProductIconEarL() != 0) {
            binding.ivEarL.setImageResource(product.getProductIconEarL());
            binding.ivEarR.setImageResource(product.getProductIconEarR());
        }
        binding.ivEarR.setVisibility(product.getProductId() == 0x000B || product.getProductId() == 0x000F ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void initData() {
        binding.setViewModel(viewModel);
        viewModel.getEqInfoLiveData().observe(this, this::updateEqUI);
        viewModel.getConnectStateLiveData().observe(this, aBoolean -> {
            if (!aBoolean) finish();
        });
    }

    private void updateEqUI(CommonEqInfo commonEqInfo) {
        int index = commonEqInfo.getIndex();

        if (index >= EqConstant.CUSTOM_INDEX_1 && binding.clInstalled.getVisibility() == View.VISIBLE) {
            binding.tvInstalled.setSelected(false);
            binding.tvAdjust.setSelected(true);
            binding.clInstalled.setVisibility(View.GONE);
            binding.clAdjust.setVisibility(View.VISIBLE);
        }

        if (index >= EqConstant.CUSTOM_INDEX_1) {
            binding.tvInstalled.setSelected(false);
            binding.tvAdjust.setSelected(true);
            binding.clInstalled.setVisibility(View.GONE);
            binding.clAdjust.setVisibility(View.VISIBLE);
            setCustomEqView(commonEqInfo.getGain());
        } else {
            binding.tvInstalled.setSelected(true);
            binding.tvAdjust.setSelected(false);
            binding.clInstalled.setVisibility(View.VISIBLE);
            binding.clAdjust.setVisibility(View.GONE);
            setEqView(commonEqInfo.getGain());
        }

        if (binding.clInstalled.getVisibility() == View.VISIBLE) {
            binding.tvEqDefault.setSelected(index == EqPreset.NATURE.getIndex());
            binding.tvEqPopular.setSelected(index == EqPreset.POP_MUSIC.getIndex());
            binding.tvEqDance.setSelected(index == EqPreset.COUNTRY_MUSIC.getIndex());
            binding.tvEqClassical.setSelected(index == EqPreset.JAZZ.getIndex());
            binding.tvEqJazz.setSelected(index == EqPreset.SLOW_SONG.getIndex());
            binding.tvEqSlow.setSelected(index == EqPreset.CLASSIC.getIndex());

            switch (index) {
                case 0 -> eqPreset = EqPreset.NATURE;
                case 1 -> eqPreset = EqPreset.POP_MUSIC;
                case 2 -> eqPreset = EqPreset.COUNTRY_MUSIC;
                case 3 -> eqPreset = EqPreset.JAZZ;
                case 4 -> eqPreset = EqPreset.SLOW_SONG;
                case 5 -> eqPreset = EqPreset.CLASSIC;
            }
        }
    }

    private void setEqView(float[] data) {
        if (binding.clInstalled.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < sbDefault.size(); i++) {
                sbDefault.get(i).setProgress(gainToProgress(data[i]));
            }
        }
    }

    private void setCustomEqView(float[] data) {
        if (binding.clAdjust.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < sbAdjust.size(); i++) {
                sbAdjust.get(i).setProgress(gainToProgress(data[i]));
            }
        }
    }

    private int gainToProgress(float gain) {
        return (int) ((gain - EqConstant.MIN_GAIN)
                / (EqConstant.MAX_GAIN - EqConstant.MIN_GAIN) * 100);
    }

    private void saveAndSetCustomEq() {
        CommonEqInfo commonEqInfo = viewModel.getEqInfoLiveData().getValue();
        if (commonEqInfo == null) {
            return;
        }
        if (commonEqInfo.getIndex() >= EqConstant.CUSTOM_INDEX_1) {
            viewModel.saveEqData(commonEqInfo.getIndex(), getEqViewData());
            viewModel.setEq(commonEqInfo.getIndex(), getEqViewData());
        }
    }

    private float[] getEqViewData() {
        float[] gain = new float[10];
        gain[0] = progressToGain(binding.icEqAdjust.sbEq1.getProgress());
        gain[1] = progressToGain(binding.icEqAdjust.sbEq2.getProgress());
        gain[2] = progressToGain(binding.icEqAdjust.sbEq3.getProgress());
        gain[3] = progressToGain(binding.icEqAdjust.sbEq4.getProgress());
        gain[4] = progressToGain(binding.icEqAdjust.sbEq5.getProgress());
        gain[5] = progressToGain(binding.icEqAdjust.sbEq6.getProgress());
        gain[6] = progressToGain(binding.icEqAdjust.sbEq7.getProgress());
        gain[7] = progressToGain(binding.icEqAdjust.sbEq8.getProgress());
        gain[8] = progressToGain(binding.icEqAdjust.sbEq9.getProgress());
        gain[9] = progressToGain(binding.icEqAdjust.sbEq10.getProgress());
        return gain;
    }

    private float progressToGain(int progress) {
        return (float) ((progress * 0.01)
                * (EqConstant.MAX_GAIN - EqConstant.MIN_GAIN)
                - EqConstant.MAX_GAIN);
    }

    @Override
    protected void initListener() {
        initSeekBar();
        binding.tvEqCustom.setOnClickListener(v -> showSelectCustomSound());
        binding.ivCustomChangeName.setOnClickListener(v -> showEditDialog());

        binding.tvInstalled.setOnClickListener(v -> {
            if (binding.clInstalled.getVisibility() == View.VISIBLE) {
                return;
            }
            binding.tvInstalled.setSelected(true);
            binding.tvAdjust.setSelected(false);
            binding.clAdjust.setVisibility(View.GONE);
            binding.clInstalled.setVisibility(View.VISIBLE);


            viewModel.setEqPreset(eqPreset == null ? EqPreset.NATURE : eqPreset);
        });

        binding.tvAdjust.setOnClickListener(v -> {
            if (binding.clAdjust.getVisibility() == View.VISIBLE) {
                return;
            }
            binding.tvInstalled.setSelected(false);
            binding.tvAdjust.setSelected(true);
            binding.clInstalled.setVisibility(View.GONE);
            binding.clAdjust.setVisibility(View.VISIBLE);

            viewModel.setEqCustom(position == 0 ? EqConstant.CUSTOM_INDEX_1 : EqConstant.CUSTOM_INDEX_2);
        });

        binding.tvEqDefault.setOnClickListener(v -> {
            if (viewModel.getEqPreset() == EqPreset.NATURE) {
                return;
            }
            viewModel.setEqPreset(EqPreset.NATURE);
        });
        binding.tvEqPopular.setOnClickListener(v -> {
            if (viewModel.getEqPreset() == EqPreset.POP_MUSIC) {
                return;
            }
            viewModel.setEqPreset(EqPreset.POP_MUSIC);
        });
        binding.tvEqDance.setOnClickListener(v -> {
            if (viewModel.getEqPreset() == EqPreset.COUNTRY_MUSIC) {
                return;
            }
            viewModel.setEqPreset(EqPreset.COUNTRY_MUSIC);
        });
        binding.tvEqClassical.setOnClickListener(v -> {
            if (viewModel.getEqPreset() == EqPreset.JAZZ) {
                return;
            }
            viewModel.setEqPreset(EqPreset.JAZZ);
        });
        binding.tvEqJazz.setOnClickListener(v -> {

            if (viewModel.getEqPreset() == EqPreset.SLOW_SONG) {
                return;
            }
            viewModel.setEqPreset(EqPreset.SLOW_SONG);
        });
        binding.tvEqSlow.setOnClickListener(v -> {
            if (viewModel.getEqPreset() == EqPreset.CLASSIC) {
                return;
            }
            viewModel.setEqPreset(EqPreset.CLASSIC);
        });
    }


    private void initSeekBar() {
        for (SeekBar seekBar : sbAdjust) {
            scroll(seekBar);
            seekBar.setOnSeekBarChangeListener(eqSeekBarChangeListener);
        }
        for (SeekBar seekBar : sbDefault) {
            seekBar.setEnabled(false);
        }
    }

    public void scroll(SeekBar seekBar) {
        seekBar.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                binding.nestedScrollview.requestDisallowInterceptTouchEvent(true);
            } else {
                binding.nestedScrollview.requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }

    private void showSelectCustomSound() {
        pickerDialogFragment.setTitle(getString(R.string.custom_sound));
        pickerDialogFragment.setData(customSounds);
        pickerDialogFragment.setDefaultPosition(position);
        pickerDialogFragment.setSelectedListener((position, str) -> {
            this.position = position;
            spModel.putData(SpConstant.CUSTOM_SOUND + "_" + deviceAddress, position);
            binding.tvEqCustom.setText(str);

            if (binding.clAdjust.getVisibility() == View.VISIBLE) {
                viewModel.setEqCustom(position == 0 ? EqConstant.CUSTOM_INDEX_1 : EqConstant.CUSTOM_INDEX_2);
            }
        });
        pickerDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    private void showEditDialog() {
        editDialogFragment.setTitle(getString(R.string.changing_the_name_of_the_set));
        editDialogFragment.setLastEditText(binding.tvEqCustom.getText().toString());
        editDialogFragment.setOnDialogClick(str -> {
            if (str.isEmpty()) {
                Toast.makeText(this, getString(R.string.custom_sound_effect_names_cannot_be_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            binding.tvEqCustom.setText(str);
            customSounds.set(position, str);
            if (position == 0) {
                spModel.putData(SpConstant.CUSTOM_SOUND_1 + "_" + deviceAddress, str);
            } else {
                spModel.putData(SpConstant.CUSTOM_SOUND_2 + "_" + deviceAddress, str);
            }

        });
        editDialogFragment.show(getSupportFragmentManager(), TAG);
    }

}
