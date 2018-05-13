package com.example.imageloader.model;

import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageRepository {

    private static final String TAG = "ImageRepository";

    private OkHttpClient mClient;

    public ImageRepository() {
        mClient = new OkHttpClient.Builder().build();
    }

    public InputStream getBitmapStream(String url) throws IOException {
        if (url == null || isLocalResource(url)) {
            Log.e(TAG, "getBitmap: illegal url");
            return null;
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }

        Request request = new Request.Builder().url(url).build();
        InputStream inputStream = null;
        try {
            Response response = mClient.newCall(request).execute();
            inputStream=response.body().byteStream();
            Log.d(TAG, "getBitmap: load bitmap from net");
        } catch (IOException e) {
            throw e;
        }

        return inputStream;
    }

    private boolean isLocalResource(String uri) {
        return uri.startsWith("/");
    }
}
