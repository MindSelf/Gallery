package com.example.zhaolexi.imageloader.view;

import com.example.zhaolexi.imageloader.bean.Detail;

public interface PhotoDetailViewInterface<V extends Detail> extends DetailViewInterface<V> {
    void showDescription();

    void hideDescription();

    void enterEditMode();

    void exitEditMode();
}
