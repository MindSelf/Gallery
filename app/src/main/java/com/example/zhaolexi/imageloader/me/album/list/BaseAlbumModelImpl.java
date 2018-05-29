package com.example.zhaolexi.imageloader.me.album.list;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.net.SendCallback;
import com.example.zhaolexi.imageloader.common.utils.AlbumConstructor;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.redirect.login.DefaultCookieJar;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class BaseAlbumModelImpl implements AlbumListModel {

    private String mAlbumUrl, mCloseUrl;
    private OkHttpClient mClient;

    public BaseAlbumModelImpl() {
        mAlbumUrl = newAlbumUrl();
        mCloseUrl = newCloseUrl();
        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
    }

    protected abstract String newCloseUrl();

    protected abstract String newAlbumUrl();


    private void onHandleData(List<Album> list) {
        AlbumConstructor constructor = new AlbumConstructor();
        for (Album album : list) {
            constructor.construct(album);
        }
    }

    @Override
    public void loadImage(int page, final OnRequestFinishListener<List<Album>> listener) {
        Request request = new Request.Builder().url(mAlbumUrl + "&currPage=" + page).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFail(e.getMessage(), null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Result<List<Album>> result = new Result<>();
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = null;
                    result.setCode(jsonObject.optInt("code"));
                    result.setMsg(jsonObject.optString("msg"));
                    JSONObject data = jsonObject.optJSONObject("data");
                    if (data != null) {
                        jsonArray = data.getJSONArray("results");
                    }
                    if (jsonArray != null) {
                        List<Album> list = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<Album>>() {
                        }.getType());
                        onHandleData(list);
                        result.setData(list);
                    }

                } catch (JSONException | IOException e) {
                    listener.onFail(BaseApplication.getContext().getString(R.string.json_error), null);
                }

                if (result.isSuccess()) {
                    listener.onSuccess(result.getData());
                } else {
                    listener.onFail("获取数据失败", result);
                }
            }
        });
    }

    @Override
    public void closeAlbum(String aid, OnRequestFinishListener listener) {
        String url = mCloseUrl.concat(aid);
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new SendCallback(listener));
    }
}
