package com.example.zhaolexi.imageloader.common.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.zhaolexi.imageloader.common.base.BaseApplication;

/**
 * Created by ZHAOLEXI on 2017/11/22.
 */

public class SharePreferencesUtils {

    private static SharedPreferences sPreference = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getContext());

    public static final String USER_NAME = "uname";
    public static final String TOKEN = "token-gallery";


    public static void putString(String key, String value) {
        SharedPreferences.Editor editor=sPreference.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key,String def) {
        return sPreference.getString(key, def);
    }

}
