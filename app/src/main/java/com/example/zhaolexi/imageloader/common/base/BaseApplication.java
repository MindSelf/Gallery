package com.example.zhaolexi.imageloader.common.base;

import org.litepal.LitePalApplication;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public class BaseApplication extends LitePalApplication {

    @Override
    public void onCreate() {
//        LeakCanary.install(this);
        super.onCreate();
    }

}
