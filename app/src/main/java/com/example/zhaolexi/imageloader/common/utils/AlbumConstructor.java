package com.example.zhaolexi.imageloader.common.utils;

import android.graphics.Bitmap;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.example.imageloader.imageloader.ImageLoader;
import com.example.imageloader.imageloader.TaskOption;
import com.example.imageloader.resizer.DecodeOption;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.home.manager.Album;

import java.io.ByteArrayOutputStream;

public class AlbumConstructor {

    private static final String TAG = "AlbumConstructor";

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

    public void setCover(Album album) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "setCover: should call in subThread");
        }
        ImageLoader imageLoader = ImageLoader.getInstance(BaseApplication.getContext());
        DecodeOption decodeOption = new DecodeOption(200);  //maxSize=2kb
        TaskOption taskOption = new TaskOption(decodeOption);
        Bitmap bitmap = imageLoader.loadBitmap(album.getCoverUrl(), taskOption);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            album.setCover(baos.toByteArray());
        }
    }

    public boolean isThird(Album album) {
        return album.getAccount() == 888888;
    }

    /**
     * 构建第三方Album字段
     *
     * @param album
     */
    private void constructThirdAlbum(Album album) {
        album.setAccessible(false);
        album.setCoverUrl("http://pic5.photophoto.cn/20071220/0020033085473726_b.jpg");
        album.setTotal(999);
    }

    /**
     * 构建Gallery Album字段
     *
     * @param album
     */
    private void constructGalleryAlbum(Album album) {
        album.setUrl(Uri.LOAD_IMG + "&album.aid=" + album.getAid() + "&currPage=");
    }

    private boolean isGalleryAlbumNeedConstructed(Album album) {
        return TextUtils.isEmpty(album.getUrl());
    }
}
