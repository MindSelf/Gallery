package com.example.zhaolexi.imageloader.me.album.list;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.home.manager.Album;

import java.util.List;

public interface AlbumListModel {
    void loadImage(int page, OnRequestFinishListener<List<Album>> listener);

    void closeAlbum(String aid, OnRequestFinishListener listener);
}
