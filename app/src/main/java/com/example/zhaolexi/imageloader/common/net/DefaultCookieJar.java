package com.example.zhaolexi.imageloader.common.net;

import com.example.zhaolexi.imageloader.common.utils.SharePreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class DefaultCookieJar implements CookieJar {

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.name().equals("token-gallery")) {
                SharePreferencesUtils.putString(SharePreferencesUtils.TOKEN, cookie.value());
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        Cookie cookie = new Cookie.Builder()
                .name(SharePreferencesUtils.TOKEN)
                .value(SharePreferencesUtils.getString(SharePreferencesUtils.TOKEN, ""))
                .domain(url.host())
                .build();
        List<Cookie> cookies = new ArrayList<>();
        cookies.add(cookie);
        return cookies;
    }
}
