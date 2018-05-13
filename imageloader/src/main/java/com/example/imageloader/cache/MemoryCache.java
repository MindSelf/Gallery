package com.example.imageloader.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.example.imageloader.Utils;
import com.example.imageloader.resizer.DecodeOption;
import com.example.imageloader.resizer.ImageResizer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 内存缓存，缓存的是压缩过的图片。存储的时候优先缓存缩略图，获取图片时需要判断该压缩图片是否满足尺寸
 */
public class MemoryCache implements ImageCache, HasMemoryCache {

    private static final String TAG = "MemoryCache";

    private LruCache<String, Bitmap> mMemoryCache;
    private ImageResizer mImageResizer;

    /**
     * 默认内存缓存参数：
     * 缓存容量为可用内存的1/4
     */
    private static final int MEMORY_CACHE_RATE = 4;

    public MemoryCache() {
        this(MEMORY_CACHE_RATE);
    }

    /**
     * @param rate 缓存容量在内存中的比率
     */
    public MemoryCache(int rate) {

        if (rate < 2) {
            throw new IllegalArgumentException("rate can't less than 2");
        }

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / rate;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            //计算缓存对象大小
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    @Override
    public Bitmap get(String url, DecodeOption option) {

        if (url == null) {
            Log.e(TAG, "ImageLoader: url can't be null");
            return null;
        }

        if (option != null && option.shouldResized) {
            Log.d(TAG, "get: should resized");
            return null;
        }

        Bitmap bitmap = mMemoryCache.get(Utils.digest(url));

        if (bitmap != null) {
            Log.d(TAG, "get: load bitmap success");
        } else {
            Log.d(TAG, "get: load bitmap fail");
        }

        return bitmap;
    }

    @Override
    public void put(String url, Bitmap bitmap, DecodeOption option) {

        if (url == null || bitmap == null) {
            Log.e(TAG, "ImageLoader: url or bitmap can't be null");
            return;
        }

        String key = Utils.digest(url);
        //优先缓存小图
        Bitmap before = mMemoryCache.get(key);
        if (option.shouldResized || (before != null && compareBitmap(before, option) <= 0)) {
            Log.d(TAG, "put: need'n be cached");
            return;
        }

        if (compareBitmap(bitmap, option) > 0) {
            //上层没进行压缩，可能是磁盘缓存失效，或者是直接从双缓存中存储
            Log.d(TAG, "put: shouldResized");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            bitmap = mImageResizer.decodeSampledBitmapFromByteArray(outputStream.toByteArray(), option);
        }
        Log.d(TAG, "put: cache success");
        mMemoryCache.put(key, bitmap);
    }

    @Override
    public void put(String url, InputStream inputStream, DecodeOption option) {
        Bitmap bitmap = mImageResizer.decodeSampledBitmapFromStream(inputStream, option);
        put(url, bitmap, option);
    }

    @Override
    public void setResizer(ImageResizer resizer) {
        mImageResizer = resizer;
    }

    @Override
    public Bitmap getInMemory(String url, DecodeOption option) {
        return get(url, option);
    }

    private int compareBitmap(Bitmap src, DecodeOption dest) {

        int reqHeight = dest.reqHeight;
        int reqWidth = dest.reqWidth;
        int maxSize = dest.maxSize;

        int bWidth = src.getWidth();
        int bHeight = src.getHeight();
        int bSize = src.getByteCount();

        if (reqWidth == 0 && reqHeight == 0 && maxSize > 0) {
            return Integer.compare(bSize, maxSize);
        }

        if (reqWidth > 0 && reqHeight > 0) {
            return Integer.compare(bWidth * bHeight, reqWidth * reqHeight);
        }

        if (reqWidth == 0 && reqHeight > 0) {
            return Integer.compare(bHeight, reqHeight);
        }

        if (reqHeight == 0 && reqWidth > 0) {
            return Integer.compare(bWidth, reqHeight);
        }

        return Integer.MAX_VALUE;
    }

    private boolean shouldResized(Bitmap bitmap, DecodeOption option) {

        if (bitmap == null) {
            Log.e(TAG, "get: load bitmap fail");
            return true;
        }

        if (option == null) {
            Log.e(TAG, "get: option can't be null");
            return true;
        }

        if (option.shouldResized) {
            Log.d(TAG, "shouldResized: option shouldResized = true");
            return true;
        }

        int reqHeight = option.reqHeight;
        int reqWidth = option.reqWidth;
        int maxSize = option.maxSize;

        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();
        int bSize = bitmap.getByteCount();

        if (reqWidth <= 0 && reqHeight <= 0 && maxSize <= 0) {
            Log.d(TAG, "shouldResized: option arguments illegal");
            return true;
        }

        if (reqHeight <= 0 && reqWidth <= 0) {
            if (maxSize < bSize) {
                Log.d(TAG, "shouldResized: bSize too large");
                return true;
            }
        }

        if (reqWidth == 0 && reqHeight > 0) {
            if (reqHeight < bHeight) {
                Log.d(TAG, "shouldResized: bHeight too large");
                return true;
            }

            if (reqHeight >= 2 * bHeight) {
                Log.d(TAG, "shouldResized: bHeight too small");
                return true;
            }
        }

        if (reqHeight == 0 && reqWidth > 0) {
            if (reqWidth < bWidth) {
                Log.d(TAG, "shouldResized: bWidth too large");
                return true;
            }

            if (reqWidth >= 2 * bWidth) {
                Log.d(TAG, "shouldResized: bWidth too small");
                return true;
            }
        }

        if (option.scaleType == DecodeOption.ScaleType.CENTER_CROP) {
            if (reqWidth / bWidth >= reqHeight / bHeight) {
                if (reqWidth < bWidth) {
                    Log.d(TAG, "shouldResized: CENTER_CROP，too large");
                    return true;
                }

                if (reqWidth >= 4 * bWidth) {
                    Log.d(TAG, "shouldResized: CENTER_CROP，too small");
                    return true;
                }
            }

            if (reqWidth / bWidth < reqHeight / bHeight) {
                if (reqHeight < bHeight) {
                    Log.d(TAG, "shouldResized: CENTER_CROP，too large");
                    return true;
                }

                if (reqHeight >= 4 * bHeight) {
                    Log.d(TAG, "shouldResized: CENTER_CROP，too small");
                }
            }
        }

        if (option.scaleType == DecodeOption.ScaleType.CENTER_INSIDE) {
            if (reqWidth / bWidth >= reqHeight / bHeight) {
                if (reqHeight < bHeight) {
                    Log.d(TAG, "shouldResized: CENTER_INSIDE，too large");
                    return true;
                }

                if (reqHeight >= 4 * bHeight) {
                    Log.d(TAG, "shouldResized: CENTER_INSIDE, too small");
                }
            }

            if (reqWidth / bWidth < reqHeight / bHeight) {
                if (reqWidth < bWidth) {
                    Log.d(TAG, "shouldResized: CENTER_INSIDE，too large");
                    return true;
                }

                if (reqWidth >= 4 * bWidth) {
                    Log.d(TAG, "shouldResized: CENTER_INSIDE，too small");
                    return true;
                }
            }
        }

        return false;
    }

}
