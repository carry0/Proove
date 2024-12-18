package com.yscoco.lib.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

@SuppressLint("MissingPermission")
public class NetworkUtils {
    private Context mContext;
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    public NetworkUtils(Context context) {
        mContext = context;
    }

    /**
     * 检查设备当前是否连接到网络。
     *
     * @return true 如果设备连接到网络，否则返回 false
     */
    public boolean isConnectedToNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = null;

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = connectivityManager.getActiveNetwork();
                if (activeNetwork != null) {
                    networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                }
            } else {
                Network[] networks = connectivityManager.getAllNetworks();
                for (Network network : networks) {
                    networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities != null) {
                        break;
                    }
                }
            }
        }

        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /**
     * 注册网络状态监听器。
     *
     * @param listener 网络状态变化的监听器
     */
    public void registerNetworkStatusListener(final NetworkStatusListener listener) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkRequest networkRequest = new NetworkRequest.Builder().build();
            mNetworkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    listener.onNetworkConnected();
                }

                @Override
                public void onLost(Network network) {
                    listener.onNetworkDisconnected();
                }
            };

            connectivityManager.registerNetworkCallback(networkRequest, mNetworkCallback);
        }
    }

    /**
     * 取消网络状态监听器的注册。
     */
    public void unregisterNetworkStatusListener() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null && mNetworkCallback != null) {
            connectivityManager.unregisterNetworkCallback(mNetworkCallback);
            mNetworkCallback = null;
        }
    }

    /**
     * 网络状态变化的监听器接口。
     */
    public interface NetworkStatusListener {
        /**
         * 当设备连接到网络时调用。
         */
        void onNetworkConnected();

        /**
         * 当设备断开网络连接时调用。
         */
        void onNetworkDisconnected();
    }
}