package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.bean.PhotoBucket;
import com.example.zhaolexi.imageloader.presenter.SelectPhotoPresenter;

import java.io.File;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface LocalPhotoModel {
    List<PhotoBucket> getBuckets();

    void uploadImg(List<File> files, SelectPhotoPresenter.OnUploadFinishListener listener);

    boolean cancel();
}
