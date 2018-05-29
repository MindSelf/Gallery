package com.example.zhaolexi.imageloader.me.album.create;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;
import com.example.zhaolexi.imageloader.home.manager.Album;

public interface AlbumCreateViewInterface extends BaseViewInterface {

    void onCreateSuccess(Album album);

    void onCreateFail(String reason);
}
