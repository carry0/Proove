package com.yscoco.lib.util;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;

import com.yscoco.lib.constant.SpConstant;

import java.util.Locale;

public class MultiLanguageUtils {

    /**
     * 修改应用内语言设置
     *
     * @param language 语言
     * @param area     地区
     */
    public static void changeLanguage(Context context, String language, String area) {
        Locale newLocale;
        boolean isDefault;
        if (TextUtils.isEmpty(language) && TextUtils.isEmpty(area)) {
            //如果语言和地区都是空，那么跟随系统
            newLocale = getSysPreferredLocale();
            isDefault = true;
        } else {
            //不为空，修改app语言，持久化语言选项信息
            newLocale = new Locale(language, area);
            isDefault = false;
        }
        setAppLanguage(context, newLocale);
        if (isDefault) {
            saveLanguageSetting(new Locale("", ""));
        } else {
            saveLanguageSetting(newLocale);
        }
    }

    private static Context setAppLanguage(Context context, Locale locale) {
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    /**
     * 跟随系统语言
     */
    public static Context attachBaseContext(Context context) {
        String spLanguage = SpUtil.getInstance().getString(SpConstant.LANGUAGE_SP_KEY, "");
        String spCountry = SpUtil.getInstance().getString(SpConstant.AREA_SP_KEY, "");
        if (!TextUtils.isEmpty(spLanguage) || !TextUtils.isEmpty(spCountry)) {
            Locale locale = new Locale(spLanguage, spCountry);
            return setAppLanguage(context, locale);
        }
        return context;
    }

    /**
     * 保存多语言信息到sp中
     */
    public static void saveLanguageSetting(Locale locale) {
        SpUtil.getInstance().putString(SpConstant.LANGUAGE_SP_KEY, locale.getLanguage());
        SpUtil.getInstance().putString(SpConstant.AREA_SP_KEY, locale.getCountry());
    }

    public static Locale getLanguageSetting() {
        String spLanguage = SpUtil.getInstance().getString(SpConstant.LANGUAGE_SP_KEY, "");
        String spCountry = SpUtil.getInstance().getString(SpConstant.AREA_SP_KEY, "");
        return new Locale(spLanguage, spCountry);
    }

    /**
     * 获取应用语言
     */
    public static Locale getAppLocale(Context context) {
        Locale local;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            local = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            local = context.getResources().getConfiguration().locale;
        }
        return local;
    }

    /**
     * 获取系统首选语言
     * <p>
     * 注意：该方法获取的是用户实际设置的不经API调整的系统首选语言
     *
     * @return Locale
     */
    public static Locale getSysPreferredLocale() {
        Locale locale;
        //7.0以下直接获取系统默认语言
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // 等同于context.getResources().getConfiguration().locale;
            locale = Locale.getDefault();
            // 7.0以上获取系统首选语言
        } else {
            /*
             * 以下两种方法等价，都是获取经API调整过的系统语言列表（可能与用户实际设置的不同）
             * 1.context.getResources().getConfiguration().getLocales()
             * 2.LocaleList.getAdjustedDefault()
             */
            // 获取用户实际设置的语言列表
            locale = LocaleList.getDefault().get(0);
        }
        return locale;
    }

    public static String getVerifyCodeLanguage(Context context) {
        Locale locale = getAppLocale(context);
        if (Locale.CHINESE.getLanguage().equals(locale.getLanguage())) {
            return "cn";
        } else {
            return "en";
        }
    }

    public static boolean isChinese(Context context) {
        return Locale.CHINESE.getLanguage().equals(getAppLocale(context).getLanguage());
    }
}