package com.example.imageloader.imageloader;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.example.imageloader.Utils;
import com.example.imageloader.cache.HasMemoryCache;
import com.example.imageloader.cache.ImageCache;
import com.example.imageloader.model.ImageRepository;
import com.example.imageloader.resizer.ImageResizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

public class ImageLoader {

    private static final String TAG = "ImageLoader";

    @SuppressLint("StaticFieldLeak")
    private static volatile ImageLoader sInstance;

    private Context mCtx;
    private int mDefaultResId, mFailResId;
    private ImageLoaderHandler mMainHandler;
    private ImageCache mImageCache;
    private ImageRepository mRepository;
    private ImageResizer mImageResizer;
    private Executor mExecutor;
    private OnLoadBitmapListener mOnLoadBitmapListener;


    private ImageLoader(Context context) {
        mCtx = context;
        mMainHandler = new ImageLoaderHandler();
    }

    public static ImageLoader getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoader(context);
                }
            }
        }
        return sInstance;
    }

    public void init(ImageLoaderConfig config) {
        mDefaultResId = config.mDefaultResId;
        mFailResId = config.mFailResId;
        mImageCache = config.mImageCache;
        mExecutor = config.mExecutor;
        mImageResizer = config.mImageResizer;
        mRepository = config.mRepository;
        mOnLoadBitmapListener = config.mOnLoadBitmapListener;
    }


    //TODO ImageLoader 在任务进行期间一直持有ImageView，直到Bitmap加载完后才释放，担心会出现OOM
    public void bindBitmap(final String uri, final ImageView imageView, final TaskOption taskOption) {

        if (uri == null) {
            Log.e(TAG, "bindBitmap: uri can't be null");
            return;
        }

        imageView.setTag(ImageLoaderHandler.TAG_KEY_URI, uri);

        //从内存缓存加载图片
        if (mImageCache instanceof HasMemoryCache) {
            HasMemoryCache memoryCache = (HasMemoryCache) mImageCache;
            Bitmap bitmap = memoryCache.getInMemory(uri, taskOption.decodeOption);
            if (bitmap != null) {
                Log.d(TAG, "bindBitmap: load bitmap in memory cache");
                imageView.setImageBitmap(bitmap);

                if (mOnLoadBitmapListener != null) {
                    mOnLoadBitmapListener.onFinish(bitmap);
                }
                return;
            }
        }


        //如果内存缓存中不存在就在线程池中加载图片
        PriorityRunnable loadBitmapTask = new PriorityRunnable(taskOption.priority) {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(uri, taskOption);

                if (bitmap != null) {
                    if (mOnLoadBitmapListener != null) {
                        mOnLoadBitmapListener.onFinish(bitmap);
                    }
                    Result result = new Result(imageView, uri, bitmap);
                    mMainHandler.obtainMessage(ImageLoaderHandler.MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };

        /*
        如果采用普通线程加载图片，随着列表的滑动可能会产生大量线程，影响整体效率
        不使用AsyncTask是因为3.0以上无法实现并发效果
         */
        mExecutor.execute(loadBitmapTask);
    }

    public Bitmap loadBitmap(String url, TaskOption taskOption) {

        Bitmap bitmap = null;
        InputStream is = null;
        try {
            bitmap = mImageCache.get(url, taskOption.decodeOption);
            if (bitmap == null) {
                Log.d(TAG, "loadBitmap: not found in cache, so load in net");
                is = mRepository.getBitmapStream(url);
                if (is != null) {
                    mImageCache.put(url, is, taskOption.decodeOption);
                    bitmap = mImageCache.get(url, taskOption.decodeOption);
                    if (bitmap == null) {
                        //缓存失效，压缩图片并返回
                        bitmap = mImageResizer.decodeSampledBitmapFromStream(is, taskOption.decodeOption);
                        Log.w(TAG, "loadBitmap: cache error! so load resized bitmap without cache");
                    }
                }
            }
            Log.d(TAG, "loadBitmap: " + bitmap);
        } catch (IOException e) {
            Log.e(TAG, "loadBitmap: ", e);
            if (mFailResId > 0) {
                bitmap = BitmapFactory.decodeResource(mCtx.getResources(), mFailResId);
            }
        }


        Utils.close(is);
        return bitmap;
    }


    public void bindDefaultImage(String url, ImageView imageView, TaskOption option) {
        if (mImageCache instanceof HasMemoryCache) {
            HasMemoryCache memoryCache = (HasMemoryCache) mImageCache;
            Bitmap bitmap = memoryCache.getInMemory(url, option.decodeOption);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                Log.d(TAG, "bindDefaultImage：bind mem-cached bitmap");
            } else if (mDefaultResId > 0) {
                Drawable def = mCtx.getResources().getDrawable(mDefaultResId);
                imageView.setImageDrawable(def);
                Log.d(TAG, "bindDefaultImage: bind default drawable");
            }
        }
    }

}
