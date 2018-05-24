package com.example.zhaolexi.imageloader.upload;

import android.support.annotation.NonNull;

import com.example.zhaolexi.imageloader.detail.Detail;

import java.io.File;

/**
 * Created by ZHAOLEXI on 2017/11/3.
 */

public class LocalPhoto implements Comparable<LocalPhoto>, Detail {

    private String thumbnailPath;

    private String path;

    public long getLastModified() {
        return new File(path).lastModified();
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int compareTo(@NonNull LocalPhoto o) {
        //最新修改的排在前面
        long src = getLastModified();
        long target = o.getLastModified();
        return Long.compare(target, src);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj != null && obj instanceof LocalPhoto) {
            return path.equals(((LocalPhoto) obj).getPath());
        }
        return false;
    }


    @Override
    public String getDetailUrl() {
        return path;
    }

    @Override
    public boolean shouldResized() {
        return thumbnailPath == null;
    }
}
