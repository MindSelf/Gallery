package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.presenter.ImagePresenter;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public interface ImageModel {

    void loadUri(int page, ImagePresenter.OnLoadFinishListener listener);

    void setUri(String newUrl);
}
