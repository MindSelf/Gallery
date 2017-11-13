package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.presenter.ImageDetailPresenter;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface ImageDetailModel {
    void loadBitmapFromDisk(String url, ImageDetailPresenter.onLoadFinishListener listener);
}
