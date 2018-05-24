package com.example.zhaolexi.imageloader.redirect.login;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;

public interface LoginModel {

    void register(String mobile, String name, String password, OnRequestFinishListener<User> listener);

    void login(String mobile, String password, OnRequestFinishListener<User> listener);

    void checkMobile(String mobile, LoginPresenter.CheckMobileCallback callback);
}
