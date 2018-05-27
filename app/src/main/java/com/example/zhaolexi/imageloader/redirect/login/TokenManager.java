package com.example.zhaolexi.imageloader.redirect.login;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.example.zhaolexi.imageloader.common.utils.SharePreferencesUtils;

import java.util.ArrayList;
import java.util.List;

public class TokenManager {

    private static final int LOG_OUT = 0;
    private static final int SIGN_IN = 1;

    private static List<OnLoginStateChangeCallback> mCallback = new ArrayList<>();
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            OnLoginStateChangeCallback callback = (OnLoginStateChangeCallback) msg.obj;
            switch (msg.what) {
                case SIGN_IN:
                    callback.onSignIn();
                    break;
                case LOG_OUT:
                    callback.onLogOut();
                    break;
            }
        }
    };

    public static void addLoginStateChangeCallback(OnLoginStateChangeCallback callback) {
        mCallback.add(callback);
    }

    public static void removeLoginStateChangeCallback(OnLoginStateChangeCallback callback) {
        mCallback.remove(callback);
    }

    public static boolean isLogin() {
        String token = SharePreferencesUtils.getString(SharePreferencesUtils.TOKEN, "");
        return !TextUtils.isEmpty(token);
    }

    public void signIn(String cookie) {
        SharePreferencesUtils.putString(SharePreferencesUtils.TOKEN, cookie);
        for (OnLoginStateChangeCallback callback : mCallback) {
            Message.obtain(mHandler, SIGN_IN, callback).sendToTarget();
        }
    }

    public void logout() {
        SharePreferencesUtils.putString(SharePreferencesUtils.TOKEN, "");
        for (OnLoginStateChangeCallback callback : mCallback) {
            Message.obtain(mHandler, LOG_OUT, callback).sendToTarget();
        }
    }
}
