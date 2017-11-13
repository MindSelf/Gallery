package com.example.zhaolexi.imageloader.view;

import android.graphics.Bitmap;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface ImageDetailViewInterface {
    void showImage(Bitmap bitmap);

    void showError(String reason);
}
