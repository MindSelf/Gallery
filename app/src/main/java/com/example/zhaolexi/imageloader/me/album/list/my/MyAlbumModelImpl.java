package com.example.zhaolexi.imageloader.me.album.list.my;

import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.me.album.list.BaseAlbumModelImpl;

public class MyAlbumModelImpl extends BaseAlbumModelImpl {

    @Override
    protected String newCloseUrl() {
        return Uri.DELETE_ALBUM;
    }

    @Override
    protected String newAlbumUrl() {
        return Uri.MY_ALBUM;
    }
}
