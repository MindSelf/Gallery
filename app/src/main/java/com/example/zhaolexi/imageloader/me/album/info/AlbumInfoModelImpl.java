package com.example.zhaolexi.imageloader.me.album.info;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.net.SendCallback;
import com.example.zhaolexi.imageloader.redirect.login.DefaultCookieJar;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class AlbumInfoModelImpl implements AlbumInfoModel {

    private OkHttpClient mClient;

    public AlbumInfoModelImpl() {
        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
    }

    @Override
    public void switchToPublic(String url, final OnRequestFinishListener listener) {
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new SendCallback(listener));
    }
}
