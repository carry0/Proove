package com.yscoco.lib.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static final String REGEX_PHONE_SIMPLE = "^[1]\\d{10}$";
    public static final String REGEX_PHONE = "^\\d{11}$";
    public static final String REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    public static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9\\W_]{5,29}$";

    public static final String REGEX_PASSWORD = "^[a-zA-Z][0-9a-zA-Z]{5,15}$";
    public static final String REGEX_LICENSE = "^[a-z0-9]{16}$";
    public static final String EMPTY = " ";
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isMatch(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean isContainSpace(String str) {
        return str.contains(" ");
    }
    public static boolean isEqual(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }
}
