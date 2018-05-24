package com.example.zhaolexi.imageloader.home.album;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public interface AlbumModel {

    void setUrl(String url);

    String getUrl();

    void loadImage(int page, OnRequestFinishListener<List<Photo>> listener);

}
