package com.example.zhaolexi.imageloader.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.zhaolexi.imageloader.base.MyApplication;

/**
 * Created by ZHAOLEXI on 2017/11/22.
 */

public class SharePreferencesUtils {

    private static SharedPreferences sPreference = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

    public static String Url = "url";
    public static String Album = "aid";

    public static void putString(String key, String value) {
        SharedPreferences.Editor editor=sPreference.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key,String def) {
        return sPreference.getString(key, def);
    }

}
