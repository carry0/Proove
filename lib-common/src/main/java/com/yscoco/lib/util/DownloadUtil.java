package com.yscoco.lib.util;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class DownloadUtil {
    public static final String TAG = "DownloadUtil";

    public interface DownloadListener {
        void onStart();

        void onProgress(int progress);

        void onFinish(File file);

        void onFailure();
    }

    public static void saveNetworkFile(String fileName, Response<ResponseBody> response, DownloadListener listener) {
        new Thread(() -> {
            LogUtil.info(TAG, "saveNetworkFile name = " + fileName);
            File file = new File(ContextUtil.getAppContext().getFilesDir(), fileName);
            listener.onStart();
            long currentLength = 0L;
            long totalLength = 0L;
            OutputStream os = null;
            InputStream is = null;
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    return;
                }
                totalLength = responseBody.contentLength();
                is = responseBody.byteStream(); //获取下载输入流
                os = ContextUtil.getAppContext().openFileOutput(file.getName(), Context.MODE_PRIVATE); //输出流
                int len;
                byte[] buff = new byte[1024];
                while ((len = is.read(buff)) != -1) {
                    os.write(buff, 0, len);
                    currentLength += len;
                    //LogUtil.info(TAG, "saveNetworkFile onProgress " + currentLength);
                    //计算当前下载百分比，并经由回调传出
                    listener.onProgress((int) (100 * currentLength / totalLength));
                    //当百分比为100时下载结束，调用结束回调，并传出下载后的本地路径
                    if ((int) (100 * currentLength / totalLength) == 100) {
                        listener.onFinish(file); //下载完成
                    }
                }
                if (totalLength == -1) {
                    listener.onFinish(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
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
            LogUtil.info(TAG, "download file size = " + file.length());
        }).start();
    }

}
