package com.proove.ble.constant;

public class SpConstant {
    public static final String AUTO_POPUP_STATE = "auto_popup_state";
    public static final String DEVICE_POPUP_STATE = "device_popup_state";
    public static final String SET_POPUP_STATE_TIME = "set_popup_state_time";
    public static final String IS_RECORD_LOCATION = "is_record_location";
    public static final String EQ_CHECKED_SP_KEY = "eq_checked";
    public static final String CURRENT_WHITE_NOISE_KEY = "current_white_noise";
    public static final String LAST_BLE_DEVICE_MAC = "";
    public static final String LAMP_CUSTOM_BT_NAME = "lamp_custom_bt_name_";
    public static final String LAMP_CUSTOM_DONGLE_NAME = "lamp_custom_dongle_name_";
    public static final String LAMP_CUSTOM_BT_COLOR = "lamp_custom_bt_color_";
    public static final String LAMP_CUSTOM_DONGLE_COLOR = "lamp_custom_dongle_color_";
    public static final String VISITOR_MODE = "visitor_mode";
    public static final String STOP_PLAY_START_TIME = "STOP_PLAY_START_TIME";
    public static final String STOP_PLAY_DURATION = "STOP_PLAY_DURATION";
    public static final String AUTO_SHUTDOWN_TIME = "AUTO_SHUTDOWN_TIME_";
    public static final String CURRENT_THEME_KEY = "CURRENT_THEME";
    public static final String SHOW_POPUP = "SHOW_POPUP";
    public static final String CUSTOM_SOUND = "CUSTOM_SOUND";
    public static final String CUSTOM_SOUND_1 = "CUSTOM_SOUND_1";
    public static final String CUSTOM_SOUND_2 = "CUSTOM_SOUND_2";
    public static String getEqCheckedSpKey(String mac) {
        return EQ_CHECKED_SP_KEY + mac.hashCode();
    }
}
