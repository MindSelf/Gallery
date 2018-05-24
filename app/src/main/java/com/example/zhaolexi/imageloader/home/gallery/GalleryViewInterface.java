package com.example.zhaolexi.imageloader.home.gallery;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;
import com.example.zhaolexi.imageloader.home.manager.ManagedAlbumAdapter;
import com.example.zhaolexi.imageloader.home.manager.Album;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/2/5.
 */

public interface GalleryViewInterface extends BaseViewInterface{

    void showAlertDialog();

    void showManagePage(boolean animated);

    void dismissManagePage();

    void showRandom(List<Album> albums);

    void showError(String reason);

    void onAlbumListStateChanged(boolean isEmpty, boolean editable);

    ManagedAlbumAdapter getAlbumAdapter();

    AlbumPagerAdapter getPagerAdapter();
}
