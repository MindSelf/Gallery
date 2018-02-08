package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.bean.PhotoBucket;
import com.example.zhaolexi.imageloader.callback.OnUploadFinishListener;

import java.io.File;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface UploadPhotoModel {
    List<PhotoBucket> getBuckets();

    void setAid(String aid);

    void uploadImg(List<File> files, OnUploadFinishListener listener);

    boolean cancel();
}
