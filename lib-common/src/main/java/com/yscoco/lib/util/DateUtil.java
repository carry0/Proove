package com.yscoco.lib.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    public static String convertAbsoluteTime(long absoluteTimeInMilliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        Date dateTime = new Date(absoluteTimeInMilliseconds);
        return formatter.format(dateTime);
    }

    public enum TimePeriod {
        MORNING,
        AFTERNOON,
        EVENING // 如果需要区分傍晚时间，可以添加此枚举项
    }

    public static TimePeriod getTimePeriod(long timestamp,TimeZone timeZone) {
        // 将时间戳转换为Calendar对象
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(timestamp);

        // 获取小时数
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // 判断并返回时间所属时段
        if (hour >= 6 && hour < 12) {
            return TimePeriod.MORNING;
        } else if (hour >= 12 && hour < 18) {
            return TimePeriod.AFTERNOON;
        } else {
            return TimePeriod.EVENING; // 或者仅保留MORNING和AFTERNOON，将18点以后视为AFTERNOON
        }
    }
}
