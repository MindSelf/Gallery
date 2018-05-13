package com.example.imageloader.cache;

import android.graphics.Bitmap;

import com.example.imageloader.resizer.DecodeOption;

public interface HasMemoryCache {

    Bitmap getInMemory(String url, DecodeOption option);
}
