package com.example.zhaolexi.imageloader.upload;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;

import java.io.File;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface UploadPhotoModel {
    List<PhotoBucket> getBuckets();

    void setAid(String aid);

    void uploadImg(List<File> files, OnRequestFinishListener listener);

    boolean cancel();
}
