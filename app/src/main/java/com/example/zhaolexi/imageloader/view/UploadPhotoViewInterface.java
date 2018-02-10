package com.example.zhaolexi.imageloader.view;

import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.bean.PhotoBucket;

import java.util.List;
import java.util.Set;

/**
 * Created by ZHAOLEXI on 2017/11/16.
 */

public interface UploadPhotoViewInterface {

    void showPhotos(Set<Photo> set);

    void openBucketList(List<PhotoBucket> list);

    void closeBucketList(boolean immediately);

    void onSelectedBucket(int position);

    void onUploadFinish(boolean success, String msg);

    String getUploadAid();
}
