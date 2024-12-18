package com.yscoco.lib.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;

/**
 * 音乐工具类,
 */
public class MusicUtil {
    private static final String TAG = "MusicUtil";

    /**
     * 格式化获取到的时间
     */
    public static String formatTime(long time) {
        if (time / 1000 % 60 < 10) {
            return time / 1000 / 60 + ":0" + time / 1000 % 60;

        } else {
            return time / 1000 / 60 + ":" + time / 1000 % 60;
        }
    }

    /**
     * 获取专辑封面
     *
     * @param context 上下文
     * @param path    歌曲路径
     * @return 封面bitmap
     */
    public static Bitmap getAlbumPicture(Context context, String path) {
        if (path == null) {
            return null;
        }
        //歌曲检索
        MediaMetadataRetriever mmr;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
        } catch (IllegalArgumentException e) {
            LogUtil.error(TAG, e.toString());
            return null;
        }
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap albumPicture = null;
        if (data != null) {
            //获取bitmap对象
            albumPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
            //获取宽高
            int width = albumPicture.getWidth();
            int height = albumPicture.getHeight();
            // 创建操作图片用的Matrix对象
            Matrix matrix = new Matrix();
            // 计算缩放比例
            float sx = ((float) 120 / width);
            float sy = ((float) 120 / height);
            // 设置缩放比例
            matrix.postScale(sx, sy);
            // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            albumPicture = Bitmap.createBitmap(albumPicture, 0, 0, width, height, matrix, false);
        }
        return albumPicture;
    }
}
