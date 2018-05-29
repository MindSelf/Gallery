package com.example.zhaolexi.imageloader.me.album.list.favorite;

import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.me.album.list.BaseAlbumModelImpl;

public class FavoriteAlbumModelImpl extends BaseAlbumModelImpl {

    @Override
    protected String newCloseUrl() {
        return Uri.COLLECT_ALBUM;
    }

    @Override
    protected String newAlbumUrl() {
        return Uri.FAVORITE_ALBUM;
    }
}
