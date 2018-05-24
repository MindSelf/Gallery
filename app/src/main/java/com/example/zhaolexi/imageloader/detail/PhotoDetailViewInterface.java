package com.example.zhaolexi.imageloader.detail;

public interface PhotoDetailViewInterface<V extends Detail> extends DetailViewInterface<V> {
    void showDescription();

    void hideDescription();

    void enterEditMode();

    void exitEditMode();
}
