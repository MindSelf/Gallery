package com.example.zhaolexi.imageloader.me.album.list;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;
import com.example.zhaolexi.imageloader.home.manager.Album;

import java.util.List;

public interface AlbumListViewInterface extends BaseViewInterface<AlbumListPresenter> {

    void onRefreshFinish();

    void showNewData(List<Album> newData);

    void showError(String hint);

    void onCloseSuccess(int pos, int type);

    void onCloseFail(String hint);
}
