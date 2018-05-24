package com.example.zhaolexi.imageloader.upload;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;
import com.example.zhaolexi.imageloader.home.manager.Album;

import java.util.Collection;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/16.
 */

public interface UploadPhotoViewInterface extends BaseViewInterface{

    void showPhotos(Collection<LocalPhoto> set);

    void openBucketList(List<PhotoBucket> list);

    void closeBucketList(boolean immediately);

    void onSelectedBucket(int position);

    void onUploadFinish(boolean success, String msg);

    Album getAlbumInfo();
}
