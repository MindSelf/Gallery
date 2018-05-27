package com.example.zhaolexi.imageloader.redirect.access;

import android.content.Context;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.ui.PasswordDialog;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.common.utils.AlbumConstructor;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.redirect.router.RedirectCallback;
import com.example.zhaolexi.imageloader.redirect.router.Router;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by ZHAOLEXI on 2018/2/7.
 */

public class AlbumPasswordDialog extends PasswordDialog<Album> {


    AlbumPasswordDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected boolean checkBeforeRequest() {
        String name = et_account.getText().toString();
        if (TextUtils.isEmpty(name) || name.contains(" ") || name.length() != 6) {
            Toast.makeText(getContext(), "请输入6位相册id", Toast.LENGTH_SHORT).show();
            return false;
        }
        return super.checkBeforeRequest();
    }

    @Override
    protected void onHandleData(Album data) {
        AlbumConstructor constructor = new AlbumConstructor();
        constructor.construct(data);
    }

    @Override
    protected Callback newCallback(String url) {
        return new AlbumCallback(url);
    }

    private final class AlbumCallback extends DefaultCallback{

        private String url;

        AlbumCallback(String url) {
            this.url = url;
        }

        @Override
        public void onResponse(Call call, Response response) {
            Log.d(TAG, "onResponse: " + response);
            try {
                Result<Album> result = new Gson().fromJson(response.body().string(), new TypeToken<Result<Album>>() {
                }.getType());
                if (result != null) {
                    if (result.isSuccess()) {
                        Message.obtain(mHandler, SUCCESS, result.getData()).sendToTarget();
                    } else {
                        Router router = new Router.Builder(getOwnerActivity())
                                .setLoginCallback(new RedirectCallback() {
                                    @Override
                                    protected void onCallback(boolean success) {
                                        if (success) {
                                            //重新登录成功后再次发起请求
                                            verify(url);
                                        }
                                    }
                                }).build();
                        if (router.route(result)) {
                            Message.obtain(mHandler, FAIL, result.getMsg()).sendToTarget();
                        }
                    }
                }
            } catch (JsonSyntaxException | IOException e) {
                Message.obtain(mHandler, FAIL, getContext().getString(R.string.json_error)).sendToTarget();
            }
        }
    }

    public static class Builder extends PasswordDialog.Builder<AlbumPasswordDialog, Album> {

        public Builder(Context context) {
            super(context);
            setVerifyUrl(Uri.ADD_ALBUM);
            setAccount("相册号", "请输入6位相册号");
            setPassword("分享码", "选填");
        }

        @Override
        protected AlbumPasswordDialog newInstance() {
            return new AlbumPasswordDialog(mCtx);
        }
    }

}
