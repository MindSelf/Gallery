package com.example.zhaolexi.imageloader.bean;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by ZHAOLEXI on 2017/11/3.
 */

public class Photo implements Comparable<Photo> {

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
    public int compareTo(@NonNull Photo o) {
        //最新修改的排在前面
        long src = getLastModified();
        long target = o.getLastModified();
        return src > target ? -1 : src < target ? 1 : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj != null && obj instanceof Photo) {
            if (path.equals(((Photo) obj).getPath())) {
                return true;
            }
        }
        return false;
    }
}
