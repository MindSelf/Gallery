package com.example.zhaolexi.imageloader.detail;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.global.Result;
import com.example.zhaolexi.imageloader.common.net.DefaultCookieJar;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.home.album.Photo;
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

public class PhotoDetailModelImpl extends DetailModelImpl<Photo> {

    private OkHttpClient mClient;

    public PhotoDetailModelImpl() {
        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
    }

    @Override
    public void loadMoreData(int page, final OnRequestFinishListener<List<Photo>> listener) {
        Request request = new Request.Builder().url(mUrl + page).build();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFail(e.getMessage(), null);
            }

            @Override
            public void onResponse(Call call, Response response) {

                Result<List<Photo>> result = new Result<>();
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = null;
                    if (jsonObject.has("error")) {
                        //第三方相册
                        boolean error = jsonObject.getBoolean("error");
                        jsonArray = jsonObject.getJSONArray("results");
                        result.setCode(error ? Result.SERVER_ERROR : Result.SUCCESS);
                    } else if (jsonObject.has("code")) {
                        //gallery album
                        jsonArray = jsonObject.getJSONObject("data").getJSONArray("results");
                        result.setCode(jsonObject.getInt("code"));
                        result.setMsg(jsonObject.getString("msg"));
                    }
                    if (jsonArray != null) {
                        List<Photo> list = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<Photo>>() {
                        }.getType());
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
}
