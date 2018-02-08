package com.example.zhaolexi.imageloader.model;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.widget.ImageView;

import com.example.zhaolexi.imageloader.base.MyApplication;
import com.example.zhaolexi.imageloader.callback.OnLoadFinishListener;
import com.example.zhaolexi.imageloader.utils.loader.ImageLoader;

import java.io.IOException;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public class ImageDetailModelImpl implements ImageDetailModel {

    private ImageLoader mImageLoader;

    public ImageDetailModelImpl() {
        mImageLoader = new ImageLoader.Builder(MyApplication.getContext()).build();
    }

    @Override
    public void loadBitmapFromDiskCache(final String url, final ImageLoader.TaskOptions options, final OnLoadFinishListener<Bitmap> listener) {
        new Thread(new Runnable() {
            Bitmap bitmap = null;
            long current = SystemClock.currentThreadTimeMillis();

            @Override
            public void run() {
                //如果列表图片还没有下载完，那么等到图片下载完并添加到磁盘缓存后再从磁盘中加载
                //这样的好处是避免重复从网络中获取图片
                while (bitmap == null && SystemClock.currentThreadTimeMillis() - current < 10000) {
                    try {
                        bitmap = mImageLoader.loadBitmapFromDisk(url, options);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bitmap != null) {
                    listener.onSuccess(bitmap);
                } else {
                    listener.onFail("加载超时");
                }
            }
        }).start();
    }

    @Override
    public void loadFullImg(String url, ImageView imageView, ImageLoader.TaskOptions options) {
        mImageLoader.bindBitmap(url, imageView, options);
    }
}
