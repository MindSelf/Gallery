package com.example.zhaolexi.imageloader.common.net;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.global.Result;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 该回调不从Response中获取Data
 */
public class SendCallback implements Callback {

    private OnRequestFinishListener listener;

    public SendCallback(OnRequestFinishListener listener) {
        this.listener = listener;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        listener.onFail(BaseApplication.getContext().getString(R.string.server_error), null);
    }

    @Override
    public void onResponse(Call call, Response response) {
        try {
            Result result = new Gson().fromJson(response.body().string(), Result.class);
            if (result.isSuccess()) {
                listener.onSuccess(null);
            } else {
                listener.onFail(result.getMsg(), result);
            }
        } catch (JsonSyntaxException | IOException e) {
            listener.onFail(BaseApplication.getContext().getString(R.string.json_error), null);
        }
    }
}
