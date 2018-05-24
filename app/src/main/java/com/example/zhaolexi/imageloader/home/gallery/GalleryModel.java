package com.example.zhaolexi.imageloader.home.gallery;

import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/2/5.
 */

public interface GalleryModel {

    List<Album> loadLocalHistory();

    void getRandom(OnRequestFinishListener<List<Album>> listener);

    void accessAlbum(int account, OnRequestFinishListener listener);

    void addAlbumToDB(Album album);

    void removeAlbumFromDB(Album album);
}
