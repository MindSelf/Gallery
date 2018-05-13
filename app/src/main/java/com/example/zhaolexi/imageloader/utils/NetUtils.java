package com.example.zhaolexi.imageloader.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetUtils {

    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null && manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null && manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }
}
