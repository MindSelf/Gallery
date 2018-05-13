package com.example.imageloader.resizer;

import android.graphics.BitmapFactory;

public class DecodeOption {
    public BitmapFactory.Options options;

    /*
     * 若同时指定maxSize和reqWidth/reqHeight，则优先满足reqWidth/reqHeight
     */
    public int maxSize;

    /*
     * 图片的目标尺寸：
     * 指定长和宽，目标尺寸取决于指定尺寸和ScaleType
     * 只指定长或宽，将根据指定尺寸按比例缩放
     * 如果都不指定，表示不对图片进行缩放
     */
    public int reqWidth;
    public int reqHeight;

    //图片的缩放模式，默认为CENTER_CROP
    public ScaleType scaleType = ScaleType.CENTER_CROP;

    //不加载内存缓存中压缩过的图片，从磁盘缓存中加载新的尺寸的图片
    public boolean shouldResized;

    public enum ScaleType {
        //按比例扩大图片的size，使得图片长(宽)等于或大于View的长(宽)，当图片长/宽超过View的长/宽，则截取图片的居中部分显示
        CENTER_CROP,
        //将图片的内容完整居中显示，通过按比例缩小或原来的size使得图片长/宽等于或小于View的长/宽
        CENTER_INSIDE
    }

    public DecodeOption(int reqWidth, int reqHeight) {
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }

    public DecodeOption(int maxSize) {
        this.maxSize = maxSize * 1024;
    }

    @Override
    public String toString() {
        return "reqWidth = " + reqWidth + "，reqHeight = " + reqHeight + "，size = " + maxSize;
    }
}
