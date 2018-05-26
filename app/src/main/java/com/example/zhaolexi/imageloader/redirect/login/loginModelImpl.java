package com.example.zhaolexi.imageloader.redirect.login;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.net.DefaultCookieJar;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.utils.SharePreferencesUtils;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class loginModelImpl implements LoginModel {

    private OkHttpClient mClient;

    public loginModelImpl() {
        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
    }

    @Override
    public void register(String mobile, String name, String password, final OnRequestFinishListener<User> listener) {
        String url = String.format(Uri.REGISTER, mobile, name, password);
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new LoginCallback(listener));
    }

    @Override
    public void login(String mobile, String password, final OnRequestFinishListener<User> listener) {
        String url = String.format(Uri.LOGIN, mobile, password);
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new LoginCallback(listener));
    }

    @Override
    public void checkMobile(String mobile, final LoginPresenter.CheckMobileCallback callback) {
        final Request request = new Request.Builder().url(Uri.CHECK_MOBILE + mobile).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onRequestFail(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Result result = new Gson().fromJson(response.body().string(), Result.class);
                    if (result.getCode() < 0) {
                        callback.onRequestFail(result.getMsg());
                    } else {
                        callback.hasRegist(result.getCode() == 0, result.getMsg());
                    }
                } catch (JsonSyntaxException | IOException e) {
                    callback.onRequestFail(BaseApplication.getContext().getString(R.string.json_error));
                }
            }
        });
    }

    class LoginCallback implements Callback {

        OnRequestFinishListener<User> listener;

        LoginCallback(OnRequestFinishListener<User> listener) {
            this.listener = listener;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            listener.onFail(e.getMessage(), null);
        }

        @Override
        public void onResponse(Call call, Response response) {
            try {
                Result<User> result = new Gson().fromJson(response.body().string(), new TypeToken<Result<User>>() {
                }.getType());
                if (result != null) {
                    if (result.isSuccess()) {
                        User user = result.getData();
                        SharePreferencesUtils.putString(SharePreferencesUtils.USER_NAME, user.getUname());
                        listener.onSuccess(user);
                    } else {
                        listener.onFail(result.getMsg(), null);
                    }
                }
            } catch (JsonSyntaxException | IOException e) {
                listener.onFail(BaseApplication.getContext().getString(R.string.json_error), null);
            }
        }
    }
}
