package com.example.imageloader.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.imageloader.resizer.DecodeOption;
import com.example.imageloader.resizer.ImageResizer;

import java.io.IOException;
import java.io.InputStream;

public class DoubleCache implements ImageCache, HasMemoryCache {

    private static final String TAG = "DoubleCache";

    private MemoryCache mMemoryCache;
    private DiskCache mDiskCache;

    public DoubleCache(Context context) {
        this(new MemoryCache(), new DiskCache(context));
    }

    public DoubleCache(MemoryCache memoryCache, DiskCache diskCache) {
        mMemoryCache = memoryCache;
        mDiskCache = diskCache;
    }

    @Override
    public Bitmap getInMemory(String url, DecodeOption option) {
        return mMemoryCache.getInMemory(url, option);
    }

    //如果双缓存都没命中的话返回null，之后将会从网络加载图片
    @Override
    public Bitmap get(String url, DecodeOption option) throws IOException {
        if (url == null) {
            Log.e(TAG, "get: url can't be null");
            return null;
        }

        Bitmap bitmap;
        bitmap = mMemoryCache.get(url, option);
        if (bitmap == null) {
            bitmap = mDiskCache.get(url, option);
            if (bitmap != null) {
                Log.d(TAG, "get: load bitmap in disk cache");
                //内存缓存没命中而磁盘缓存命中，可能是由于缓存被清除，从磁盘缓存获取图片后存储到内存缓存中
                mMemoryCache.put(url, bitmap, option);
            }
        } else {
            Log.d(TAG, "get: load bitmap in memory cache");
        }
        return bitmap;
    }

    @Override
    public void setResizer(ImageResizer resizer) {
        mMemoryCache.setResizer(resizer);
        mDiskCache.setResizer(resizer);
    }

    //从网络加载图片后，原图缓存到DiskCache中，然后再次从双缓存中获取
    @Override
    public void put(String url, Bitmap bitmap, DecodeOption option) throws IOException {
        Log.d(TAG, "put in double cache");
        mDiskCache.put(url, bitmap, option);
        mMemoryCache.put(url, bitmap, option);
    }

    @Override
    public void put(String url, InputStream inputStream, DecodeOption option) throws IOException {
        Log.d(TAG, "put in double cache");
        mDiskCache.put(url, inputStream, option);
        mMemoryCache.put(url, inputStream, option);
    }
}
