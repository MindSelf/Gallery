package com.example.imageloader.imageloader;

import android.content.Context;

import com.example.imageloader.R;
import com.example.imageloader.cache.DoubleCache;
import com.example.imageloader.cache.ImageCache;
import com.example.imageloader.model.ImageRepository;
import com.example.imageloader.resizer.ImageResizer;
import com.example.imageloader.thread.ImageLoaderExecutor;

import java.util.concurrent.Executor;

public class ImageLoaderConfig {

    int mDefaultResId, mFailResId;
    ImageCache mImageCache;
    ImageRepository mRepository;
    ImageResizer mImageResizer;
    Executor mExecutor;
    OnLoadBitmapListener mOnLoadBitmapListener;

    private ImageLoaderConfig() {

    }

    public static class Builder{

        int mDefaultResId, mFailResId;
        ImageCache mImageCache;
        ImageResizer mImageResizer;
        ImageRepository mRepository;
        Executor mExecutor;
        OnLoadBitmapListener mOnLoadBitmapListener;

        public Builder(Context context) {
            /*
            默认配置
             */
            mDefaultResId = R.drawable.image_default;
            mFailResId = R.drawable.image_fail;
            mImageResizer = new ImageResizer();
            mImageCache = new DoubleCache(context);
            mImageCache.setResizer(mImageResizer);
            mRepository = new ImageRepository();
            mExecutor = new ImageLoaderExecutor();
        }

        public Builder setDefaultImage(int defaultImage) {
            this.mDefaultResId = defaultImage;
            return this;
        }

        public Builder setFailImage(int failImage) {
            this.mFailResId = failImage;
            return this;
        }

        public Builder setImageCache(ImageCache imageCache) {
            this.mImageCache = imageCache;
            return this;
        }

        public Builder setImageResizer(ImageResizer imageResizer) {
            this.mImageResizer = imageResizer;
            return this;
        }

        public Builder setRepository(ImageRepository repository) {
            this.mRepository = repository;
            return this;
        }

        public Builder setExecutor(Executor executor) {
            this.mExecutor = executor;
            return this;
        }

        public Builder setOnLoadBitmapListener(OnLoadBitmapListener onLoadBitmapListener) {
            this.mOnLoadBitmapListener = onLoadBitmapListener;
            return this;
        }

        void applyConfig(ImageLoaderConfig config) {
            config.mDefaultResId = mDefaultResId;
            config.mFailResId = mFailResId;
            config.mImageCache = mImageCache;
            config.mExecutor = mExecutor;
            config.mImageResizer = mImageResizer;
            config.mRepository = mRepository;
            config.mOnLoadBitmapListener = mOnLoadBitmapListener;
        }

        public ImageLoaderConfig build() {
            ImageLoaderConfig config = new ImageLoaderConfig();
            applyConfig(config);
            return config;
        }
    }
}
