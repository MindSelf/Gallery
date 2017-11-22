package com.example.zhaolexi.imageloader.view;

import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.bean.PhotoBucket;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/16.
 */

public interface SelectPhotoViewInterface {

    void showPhotos(List<Photo> list);

    void openBucketList(List<PhotoBucket> list);

    void closeBucketList(boolean immediately);

    void changeSelectedBucket(int position);

    void onUploadFinish(boolean success, String msg);
}
