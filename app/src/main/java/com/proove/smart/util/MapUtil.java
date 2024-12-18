package com.proove.smart.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.proove.ble.data.SimpleLocation;
import com.proove.smart.R;
import com.yscoco.lib.util.ContextUtil;
import com.yscoco.lib.util.StringUtil;
import com.yscoco.lib.util.ToastUtil;

public class MapUtil {
    public static final String BAIDUMAP_PACKAGENAME = "com.baidu.BaiduMap";
    public static final String GAODEMAP_PACKAGENAME = "com.autonavi.minimap";
    public static final String QQMAP_PACKAGENAME = "com.tencent.map";
    public static final String GOOGLE_MAP_PACKAGENAME = "com.google.android.apps.maps";

    public enum CoordType {
        WGS84,
        GCJ02,
        BD09
    }


//    public static String formatDistance(double distance) {
//        long d = Math.round(distance);
//        if (d <= 0) {
//            return 0 + ContextUtil.getAppContext().getString(R.string.distance_m);
//        } else if (d < 1000) {
//            return d + ContextUtil.getAppContext().getString(R.string.distance_m);
//        } else {
//            return d / 1000 + ContextUtil.getAppContext().getString(R.string.distance_km);
//        }
//    }

    public static void jumpMapApp(Context context, String packageName, double longitude, double latitude, CoordType coordType) {
        if (context == null || StringUtil.isNullOrEmpty(packageName) || coordType == null) {
            return;
        }
        StringBuilder uriSb = new StringBuilder();
        SimpleLocation wgs84Location;
        SimpleLocation bd09Location;
        SimpleLocation gcj02Location;
        switch (coordType) {
            case WGS84 -> {
                wgs84Location = new SimpleLocation(longitude, latitude);
                bd09Location = wgs84tobd09(longitude, latitude);
                gcj02Location = wgs84ToGcj02(longitude, latitude);
            }
            case GCJ02 -> {
                wgs84Location = gcj02ToWgs84(longitude, latitude);
                bd09Location = gcj02ToBd09(longitude, latitude);
                gcj02Location = new SimpleLocation(longitude, latitude);
            }
            case BD09 -> {
                wgs84Location = bd09ToWgs84(longitude, latitude);
                bd09Location = new SimpleLocation(longitude, latitude);
                gcj02Location = bd09ToGcj02(longitude, latitude);
            }
            default -> {
                wgs84Location = null;
                bd09Location = null;
                gcj02Location = null;
            }
        }
        if (GAODEMAP_PACKAGENAME.equals(packageName)) {
            //高德
            uriSb.append("androidamap://viewReGeo?");
            uriSb.append("sourceApplication=").append(getAppPackageName());
            uriSb.append("&lat=").append(gcj02Location.getLatitude());
            uriSb.append("&lon=").append(gcj02Location.getLongitude());
            uriSb.append("&dev=").append("0");
        } else if (BAIDUMAP_PACKAGENAME.equals(packageName)) {
            //百度
            uriSb.append("baidumap://map/geocoder?");
            uriSb.append("location=").append(bd09Location.getLatitude())
                    .append(",").append(bd09Location.getLongitude());
            uriSb.append("&coord_type=").append("bd09ll");
            uriSb.append("&src=").append(getAppPackageName());
        } else if (QQMAP_PACKAGENAME.equals(packageName)) {
            //腾讯
            uriSb.append("qqmap://map/routeplan?");
            uriSb.append("type=").append("drive");
            uriSb.append("&fromcoord=").append("CurrentLocation");
            uriSb.append("&tocoord=").append(gcj02Location.getLatitude())
                    .append(",").append(gcj02Location.getLongitude());
            uriSb.append("&referer=").append("CQIBZ-WBNLX-75E4I-ZI4DT-OEERZ-TNFNT");
        } else if (GOOGLE_MAP_PACKAGENAME.equals(packageName)) {
            uriSb.append("google.navigation:q=").append(wgs84Location.getLatitude())
                    .append(",").append(longitude);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.parse(uriSb.toString());
        intent.setData(uri);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastUtil.showToast(ContextUtil.getAppContext(),
                    ContextUtil.getAppContext().getString(R.string.no_map_app));
        }
    }

    public static String getAppPackageName() {
        return ContextUtil.getAppContext().getPackageName();
    }

    static double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
    static double PI = 3.1415926535897932384626;
    static double a = 6378245.0;
    static double ee = 0.00669342162296594323;

    /**
     * 百度坐标系 (BD-09) 与 火星坐标系 (GCJ-02)的转换
     * 即 百度 转 谷歌、高德
     *
     * @param bd_lon
     * @param bd_lat
     * @returns {*[]}
     */
    public static SimpleLocation bd09ToGcj02(double bd_lon, double bd_lat) {
        double x = bd_lon - 0.0065;
        double y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_PI);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new SimpleLocation(gg_lng, gg_lat);
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换
     * 即谷歌、高德 转 百度
     *
     * @param lng
     * @param lat
     * @returns {*[]}
     */
    public static SimpleLocation gcj02ToBd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * x_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * x_PI);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new SimpleLocation(bd_lng, bd_lat);
    }

    ;

    /**
     * WGS84转GCj02
     *
     * @param lng
     * @param lat
     * @returns {*[]}
     */
    public static SimpleLocation wgs84ToGcj02(double lng, double lat) {
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * PI;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new SimpleLocation(mglng, mglat);
    }

    ;

    /**
     * GCJ02 转换为 WGS84
     *
     * @param lng
     * @param lat
     * @returns {*[]}
     */
    public static SimpleLocation gcj02ToWgs84(double lng, double lat) {
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * PI;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new SimpleLocation(mglng, mglat);
    }

    ;

    /**
     * WGS84 转换为 BD-09
     *
     * @param lng
     * @param lat
     * @returns {*[]}
     */
    private static SimpleLocation wgs84tobd09(double lng, double lat) {
        // 第一次转换
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * PI;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI);
        double mglat = lat + dlat;
        double mglng = lng + dlng;

        // 第二次转换
        double z = Math.sqrt(mglng * mglng + mglat * mglat) + 0.00002 * Math.sin(mglat * x_PI);
        double theta = Math.atan2(mglat, mglng) + 0.000003 * Math.cos(mglng * x_PI);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new SimpleLocation(bd_lng, bd_lat);
    }

    private static SimpleLocation bd09ToWgs84(double lng, double lat) {
        SimpleLocation gcj02 = bd09ToGcj02(lng, lat);
        SimpleLocation wgs84 = gcj02ToWgs84(gcj02.getLongitude(), gcj02.getLatitude());
        return wgs84;
    }

    private static double transformlat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformlng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
}
