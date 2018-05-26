package com.example.zhaolexi.imageloader.home.manager;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;

import java.util.List;

public interface AlbumManageModel {

    void getRandom(final OnRequestFinishListener<List<Album>> listener);

    void addAlbumToDB(Album album);

    void removeAlbumFromDB(Album album);
}
