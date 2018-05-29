package com.example.zhaolexi.imageloader.home.album;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;
import com.example.zhaolexi.imageloader.home.manager.Album;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public interface AlbumViewInterface extends BaseViewInterface{

    void showNewData(boolean hasMore, List<Photo> newData);

    void showError();

    void showLoading();

    void collectSuccess(String msg);

    void collectFail(String msg);

    void showAlertDialog();

    void setRefreshing(boolean isRefreshing);

    void onRefreshFinish();

    Album getAlbumInfo();
}
