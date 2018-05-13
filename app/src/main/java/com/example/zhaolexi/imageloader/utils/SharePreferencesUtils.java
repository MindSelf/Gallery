package com.example.zhaolexi.imageloader.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.zhaolexi.imageloader.base.BaseApplication;

/**
 * Created by ZHAOLEXI on 2017/11/22.
 */

public class SharePreferencesUtils {

    private static SharedPreferences sPreference = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getContext());

    @Deprecated
    public static String Url = "url";
    @Deprecated
    public static String Album = "aid";
    public static String User = "usr";

    public static void putString(String key, String value) {
        SharedPreferences.Editor editor=sPreference.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key,String def) {
        return sPreference.getString(key, def);
    }

}
