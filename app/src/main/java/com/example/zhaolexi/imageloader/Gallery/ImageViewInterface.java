package com.example.zhaolexi.imageloader.Gallery;

import com.example.zhaolexi.imageloader.bean.Image;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public interface ImageViewInterface {

    void showNewDatas(List<Image> newDatas);

    void showError();

    void showNoData();

    void showLoading();
}
