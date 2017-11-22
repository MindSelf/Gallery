package com.example.zhaolexi.imageloader.view;

import com.example.zhaolexi.imageloader.bean.Image;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public interface GalleryViewInterface {

    void showNewDatas(boolean hasMore,List<Image> newDatas);

    void showError();

    void showLoading();

}
