package com.example.zhaolexi.imageloader.home.gallery;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.global.Result;
import com.example.zhaolexi.imageloader.common.net.DefaultCookieJar;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ZHAOLEXI on 2018/2/5.
 */

public class GalleryModelImpl implements GalleryModel {

    private OkHttpClient mClient;

    public GalleryModelImpl() {
        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
    }

    @Override
    public List<Album> loadLocalHistory() {
        Connector.getDatabase();    //数据库不存在则创建数据库
        return DataSupport.findAll(Album.class);
    }

    @Override
    public void getRandom(final OnRequestFinishListener<List<Album>> listener) {
        Request request = new Request.Builder().url(Uri.GET_RANDOM).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFail(e.getMessage(), null);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Result<List<Album>> result = new Gson().fromJson(response.body().string(),
                            new TypeToken<Result<List<Album>>>() {
                            }.getType());
                    if (result != null) {
                        if (result.isSuccess()) {
                            listener.onSuccess(result.getData());
                        } else {
                            listener.onFail(result.getMsg(), result);
                        }
                    }
                } catch (JsonSyntaxException | IOException e) {
                    listener.onFail(BaseApplication.getContext().getString(R.string.json_error), null);
                }
            }
        });
    }

    @Override
    public void accessAlbum(int account, final OnRequestFinishListener listener) {
        Request request = new Request.Builder().url(String.format(Uri.ADD_ALBUM, String.valueOf(account), "")).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Result result = new Gson().fromJson(response.body().string(), Result.class);
                if (result.isSuccess()) {
                    listener.onSuccess(null);
                }
            }
        });
    }

    @Override
    public void addAlbumToDB(Album album) {
        album.save();
    }

    @Override
    public void removeAlbumFromDB(Album album) {
        album.delete();
    }
}
