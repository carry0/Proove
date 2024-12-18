package com.yscoco.lib.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class AssetUtil {

    /**
     * 读取assets本地json
     * @param fileName
     * @param context
     * @return
     */
    public static String getJson(String fileName, Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static File getFile(Context context, String fileName) {
        File file = new File(ContextUtil.getAppContext().getFilesDir(), fileName);
        InputStream is = null;
        OutputStream os = null;
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            is = assetManager.open(fileName);
            os = context.openFileOutput(file.getName(), Context.MODE_PRIVATE);
            int len;
            byte[] buff = new byte[1024];
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            if (os != null) {
                try {
                    os.close(); //关闭输出流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close(); //关闭输入流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }
}
