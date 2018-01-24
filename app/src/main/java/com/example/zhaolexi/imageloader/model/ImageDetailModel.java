package com.example.zhaolexi.imageloader.model;

import android.widget.ImageView;

import com.example.zhaolexi.imageloader.presenter.ImageDetailPresenter;
import com.example.zhaolexi.imageloader.utils.loader.ImageLoader;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface ImageDetailModel {

    void loadBitmapFromDiskCache(String url, ImageLoader.TaskOptions options,
                                 ImageDetailPresenter.onLoadFinishListener listener);

    void loadFullImg(String url, ImageView imageView, ImageLoader.TaskOptions options);
}
