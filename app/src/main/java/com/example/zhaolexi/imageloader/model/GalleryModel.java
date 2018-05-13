package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.bean.Album;
import com.example.zhaolexi.imageloader.callback.OnLoadFinishListener;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/2/5.
 */

public interface GalleryModel {

    List<Album> loadLocalHistory();

    void getRandom(OnLoadFinishListener<Album> listener);

    void addAlbumToDB(Album album);

    void removeAlbumFromDB(Album album);
}
