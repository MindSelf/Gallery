package com.example.zhaolexi.imageloader.common.utils;

import android.text.TextUtils;

import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.home.manager.Album;

public class AlbumConstructor {

    public void construct(Album album) {
        if (isGalleryAlbumNeedConstructed(album)) {
            //gallery album
            constructGalleryAlbum(album);
        }
        if (isThird(album)) {
            //第三方相册
            constructThirdAlbum(album);
        }
    }

    public boolean isThird(Album album) {
        return album.getAccount() == 888888;
    }

    /**
     * 构建第三方Album字段
     * @param album
     */
    private void constructThirdAlbum(Album album) {
        album.setAccessible(false);
        album.setCoverUrl("http://pic5.photophoto.cn/20071220/0020033085473726_b.jpg");
        album.setTotal(999);
    }

    /**
     * 构建Gallery Album字段
     * @param album
     */
    private void constructGalleryAlbum(Album album) {
        album.setUrl(Uri.LOAD_IMG + "&album.aid=" + album.getAid() + "&currPage=");
    }

    private boolean isGalleryAlbumNeedConstructed(Album album) {
        return TextUtils.isEmpty(album.getUrl());
    }
}
