package com.example.zhaolexi.imageloader.home.manager;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;
import com.example.zhaolexi.imageloader.home.gallery.AlbumPagerAdapter;

import java.util.List;

public interface AlbumManageViewInterface extends BaseViewInterface<AlbumManagePresenter> {

    void attachPresenter();

    void detachPresenter();

    void showError(String reason);

    void showManagePage(boolean animated);

    void dismissManagePage();

    void showRandom(List<Album> albums);

    void onAlbumListStateChanged(boolean isEmpty, boolean editable);

    void onAlbumClear();

    /**
     *
     * @return is consumed
     */
    boolean onBackPressed();

    ManagedAlbumAdapter getAlbumAdapter();

    AlbumPagerAdapter getPagerAdapter();
}
