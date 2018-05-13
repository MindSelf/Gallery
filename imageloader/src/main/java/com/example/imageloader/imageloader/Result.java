package com.example.imageloader.imageloader;

import android.graphics.Bitmap;
import android.widget.ImageView;

/*
 * 对加载图片结果的封装
 */
public class Result {
    ImageView imageView;
    String uri;
    Bitmap bitmap;

    Result(ImageView imageView, String uri, Bitmap bitmap) {
        this.imageView = imageView;
        this.uri = uri;
        this.bitmap = bitmap;
    }
}
