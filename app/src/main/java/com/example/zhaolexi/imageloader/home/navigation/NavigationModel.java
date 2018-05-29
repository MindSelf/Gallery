package com.example.zhaolexi.imageloader.home.navigation;

import com.example.zhaolexi.imageloader.home.manager.Album;

import java.util.List;

public interface NavigationModel {

    void loadAlbumInfo();

    void releaseAlbumInfo();

    List<Album> getMyAlbums();

    List<Album> getFavoriteAlbums();
}
