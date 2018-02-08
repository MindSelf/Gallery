package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.presenter.AlbumPresenter;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public interface AlbumModel {

    void setUrl(String url);

    void loadImage(int page, AlbumPresenter.OnLoadFinishListener listener);

}
