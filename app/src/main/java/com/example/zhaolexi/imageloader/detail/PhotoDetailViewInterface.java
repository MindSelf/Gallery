package com.example.zhaolexi.imageloader.detail;

import com.example.zhaolexi.imageloader.home.manager.Album;

public interface PhotoDetailViewInterface<V extends Detail> extends DetailViewInterface<V> {
    void showDescription();

    void hideDescription();

    void enterEditMode();

    void exitEditMode();

    void deletePhoto();

    Album getAlbumInfo();

    void showHint(String hint);
}
