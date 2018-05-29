package com.example.zhaolexi.imageloader.me.album.create;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.utils.AlbumConstructor;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.redirect.login.DefaultCookieJar;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlbumCreateModelImpl implements AlbumCreateModel {

    private OkHttpClient mClient;

    public AlbumCreateModelImpl() {
        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
    }

    @Override
    public void createAlbum(String url, final OnRequestFinishListener<Album> listener) {
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFail(e.getMessage(), null);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Result<Album> result = new Gson().fromJson(response.body().string(), new TypeToken<Result<Album>>() {
                    }.getType());
                    if (result.isSuccess()) {
                        Album album = result.getData();
                        new AlbumConstructor().construct(album);
                        listener.onSuccess(album);
                    } else {
                        listener.onFail(result.getMsg(), result);
                    }
                } catch (JsonSyntaxException | IOException e) {
                    listener.onFail(BaseApplication.getContext().getString(R.string.json_error), null);
                }
            }
        });
    }
}
