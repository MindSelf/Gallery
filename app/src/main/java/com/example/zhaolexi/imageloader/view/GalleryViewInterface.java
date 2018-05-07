package com.example.zhaolexi.imageloader.view;

import com.example.zhaolexi.imageloader.adapter.AlbumPagerAdapter;
import com.example.zhaolexi.imageloader.adapter.ManagedAlbumAdapter;
import com.example.zhaolexi.imageloader.bean.Album;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/2/5.
 */

public interface GalleryViewInterface {

    void showAlertDialog();

    void showManagePage(boolean animated);

    void dismissManagePage();

    void showRandom(List<Album> albums);

    void showError(String reason);

    void onAlbumListStateChanged(boolean isEmpty, boolean editable);

    ManagedAlbumAdapter getAlbumAdapter();

    AlbumPagerAdapter getPagerAdapter();
}
