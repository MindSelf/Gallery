package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.callback.OnLoadFinishListener;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public interface AlbumModel {

    void setUrl(String url);

    String getUrl();

    void loadImage(int page, OnLoadFinishListener<Photo> listener);

}
