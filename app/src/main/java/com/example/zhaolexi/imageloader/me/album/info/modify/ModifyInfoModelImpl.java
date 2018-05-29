package com.example.zhaolexi.imageloader.me.album.info.modify;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.net.SendCallback;
import com.example.zhaolexi.imageloader.redirect.login.DefaultCookieJar;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ModifyInfoModelImpl implements ModifyInfoModel{

    private OkHttpClient mClient;
    private String mUrl;

    public ModifyInfoModelImpl() {
        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
    }

    @Override
    public void setUri(String uri) {
        mUrl = uri;
    }

    @Override
    public void modify(String text, OnRequestFinishListener listener) {
        Request request = new Request.Builder().url(String.format(mUrl, text)).build();
        mClient.newCall(request).enqueue(new SendCallback(listener));
    }
}
