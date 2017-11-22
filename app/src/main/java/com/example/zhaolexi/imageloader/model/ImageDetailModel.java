package com.example.zhaolexi.imageloader.model;

import android.widget.ImageView;

import com.example.zhaolexi.imageloader.presenter.ImageDetailPresenter;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface ImageDetailModel {

    void loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight,
                                 ImageDetailPresenter.onLoadFinishListener listener);

    void loadFullImg(String url, ImageView imageView, int reqWidth, int reqHeight);
}
