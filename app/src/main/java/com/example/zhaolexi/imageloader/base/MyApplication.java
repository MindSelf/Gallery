package com.example.zhaolexi.imageloader.base;

import android.app.Application;
import android.content.Context;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        context=getApplicationContext();
//        LeakCanary.install(this);
        super.onCreate();
    }

    public static Context getContext() {
        return context;
    }
}
