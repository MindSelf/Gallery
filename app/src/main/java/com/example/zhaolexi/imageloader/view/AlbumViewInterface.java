package com.example.zhaolexi.imageloader.view;

import com.example.zhaolexi.imageloader.adapter.ImageAdapter;
import com.example.zhaolexi.imageloader.bean.Album;
import com.example.zhaolexi.imageloader.bean.Image;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public interface AlbumViewInterface {

    void showNewData(boolean hasMore, List<Image> newData);

    void showError();

    void showLoading();

    void setRefreshing(boolean isRefreshing);

    void showAlertDialog();

    ImageAdapter getAdapter();

    Album getAlbumInfo();
}
