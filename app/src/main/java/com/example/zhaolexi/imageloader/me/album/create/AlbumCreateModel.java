package com.example.zhaolexi.imageloader.me.album.create;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.home.manager.Album;

public interface AlbumCreateModel {
    void createAlbum(String url, OnRequestFinishListener<Album> listener);
}
