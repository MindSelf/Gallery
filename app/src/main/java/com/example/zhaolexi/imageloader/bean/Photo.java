package com.example.zhaolexi.imageloader.bean;

/**
 * Created by ZHAOLEXI on 2017/11/3.
 */

public class Photo {

    private String thumbnailPath;

    private String path;

    private boolean isSelected;

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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
