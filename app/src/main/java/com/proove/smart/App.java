package com.proove.smart;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.jieli.bluetooth.bean.BluetoothOption;
import com.jieli.bluetooth.impl.rcsp.RCSPController;
import com.proove.ble.constant.Constant;
import com.proove.smart.manager.ActivityManager;
import com.proove.smart.manager.BluetoothManager;
import com.proove.smart.manager.DeviceManager;
import com.proove.smart.util.SpModel;
import com.yscoco.lib.constant.SpConstant;
import com.yscoco.lib.util.ContextUtil;
import com.yscoco.lib.util.SpUtil;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ContextUtil.setAppContext(this);
        if (!RCSPController.isInit()) {
            BluetoothManager.getInstance().init(this);
            BluetoothOption bluetoothOption = BluetoothOption.createDefaultOption()
                    .setPriority(BluetoothOption.PREFER_SPP)
                    .setTimeoutMs(10000)
                    .setUseDeviceAuth(true);
            RCSPController.init(this, bluetoothOption);
        }
        registerActivityLifecycleCallbacks(ActivityManager.getInstance());
//        YSAppSDK.getInstance().init(this, Constant.APP_ID, Constant.APP_KEY);
    }


}
