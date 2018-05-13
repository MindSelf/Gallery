package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.callback.OnLoadFinishListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public class AlbumModelImpl implements AlbumModel {

    private OkHttpClient mClient;
    private String mUrl;

    public AlbumModelImpl() {
        mClient = new OkHttpClient.Builder().build();
    }

    @Override
    public void setUrl(String url) {
        mUrl=url;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public void loadImage(int currentPage, final OnLoadFinishListener<Photo> listener) {
        Request request=new Request.Builder().url(mUrl +currentPage).build();
        Call call= mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFail(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (!jsonObject.getBoolean("error")) {
                        JSONArray result = jsonObject.getJSONArray("results");
                        List<Photo> newData = new ArrayList<>();
                        Gson gson = new Gson();
                        for (int i = 0; i < result.length(); i++) {
                            newData.add(gson.fromJson(result.getJSONObject(i).toString(), Photo.class));
                        }
                        listener.onSuccess(newData);
                    } else {
                        listener.onFail("获取数据失败");
                    }
                } catch (JSONException e) {
                    listener.onFail(e.getMessage());
                }
            }
        });
    }

}
