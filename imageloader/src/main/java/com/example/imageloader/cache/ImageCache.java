package com.example.imageloader.cache;

import android.graphics.Bitmap;

import com.example.imageloader.resizer.DecodeOption;
import com.example.imageloader.resizer.ImageResizer;

import java.io.IOException;
import java.io.InputStream;

public interface ImageCache {

    Bitmap get(String url, DecodeOption option) throws IOException;

    void put(String url, Bitmap bitmap, DecodeOption option) throws IOException;

    void put(String url, InputStream inputStream, DecodeOption option) throws IOException;

    void setResizer(ImageResizer resizer);
}
