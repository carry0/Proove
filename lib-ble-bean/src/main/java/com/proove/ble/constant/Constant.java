package com.proove.ble.constant;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class Constant {
    public static final String APP_DATABASE_NAME = "app_database";

    public static final String BUGLY_SDK_APIKEY = "21a2cdc2e4";

//    public static final String APP_DATABASE_NAME = "app_database";
//
//    public static final String BUGLY_SDK_APIKEY = "e0e7dd4fd4";

    // Android11新增权限
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static final String[] PERMISSIONS_V31 = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION,
            BLUETOOTH_SCAN, BLUETOOTH_CONNECT};
    // 应用所需动态申请权限
    public static final String[] PERMISSIONS = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};

    public static final int DEVICE_NAME_LIMIT = 32;
    public static final int BLE_MTU = 255;
    public static final String APP_TABLE_NAME = "Proove";

    public static int COMPANY_ID = 0xF622;

    public static int ID = 0xFFFF;

//    public static int COMPANY_ID = 0xE022;
//public static int COMPANY_ID = 0xED22;
    public static final String BASE_URL = "https://prooveglobal.com";

    public static final String USER_AGREEMENT_ZH = BASE_URL + "/oferta";
    public static final String USER_AGREEMENT_EN = BASE_URL + "/oferta";
    public static final String PRIVACY_POLICY_ZH = BASE_URL + "/privacy-policy";
    public static final String PRIVACY_POLICY_EN = BASE_URL + "/privacy-policy";
    public static final String ABOUT_US_ZH = BASE_URL + "/";
    public static final String ABOUT_US_EN = BASE_URL + "/";
    public static final String OPERATING_MANUAL_ZH = BASE_URL + "/index.php/home/web/about?lang=zh";
    public static final String OPERATING_MANUAL_EN = BASE_URL + "/index.php/home/web/about?lang=en";

    public static final String FIRMWARE_UPGRADE_FILE_NAME_TAG = "firmware_upgrade_%s_%s_%s";

    public static final String APP_ID = "11de7ce7bc011af9793126cb4d637034";
    public static final String APP_KEY = "8f69800d3e7c0b32ee889b8804a061ed";
    //public static final String APP_ID = "b470590ca592921017b375bea84ed5ee";
//    public static final String APP_KEY = "ed9055556aec7321cdfe0ebb83faef5d";
    public static final long BLOCK_TIME = 15000;
}
