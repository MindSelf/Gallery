package com.example.zhaolexi.imageloader.presenter;

import android.content.Intent;
import android.os.Message;

import com.example.zhaolexi.imageloader.base.BasePresenter;
import com.example.zhaolexi.imageloader.bean.LocalPhoto;
import com.example.zhaolexi.imageloader.bean.PhotoBucket;
import com.example.zhaolexi.imageloader.callback.OnUploadFinishListener;
import com.example.zhaolexi.imageloader.model.UploadPhotoModel;
import com.example.zhaolexi.imageloader.model.UploadPhotoModelImpl;
import com.example.zhaolexi.imageloader.view.DetailActivity;
import com.example.zhaolexi.imageloader.view.LocalDetailActivity;
import com.example.zhaolexi.imageloader.view.UploadPhotoActivity;
import com.example.zhaolexi.imageloader.view.UploadPhotoViewInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ZHAOLEXI on 2017/11/16.
 */

public class UploadPhotoPresenter extends BasePresenter<UploadPhotoViewInterface, UploadPhotoModel> {

    public static final int OPEN_LOCAL_PHOTO_DETAIL = 1;

    private boolean mIsListOpen;

    @Override
    protected UploadPhotoModel newModel() {
        return new UploadPhotoModelImpl();
    }

    @Override
    public void attachView(UploadPhotoViewInterface view) {
        super.attachView(view);
        mModel.setAid(view.getUploadAid());
    }

    @Override
    protected void onMessageSuccess(Message msg) {
        UploadPhotoViewInterface mView = getView();
        mView.onUploadFinish(true, (String) msg.obj);
    }

    @Override
    protected void onMessageFail(Message msg) {
        UploadPhotoViewInterface mView = getView();
        mView.onUploadFinish(false, (String) msg.obj);
    }

    public void chooseBucketList() {
        if (isViewAttached()) {
            UploadPhotoViewInterface mView = getView();
            if (!mIsListOpen) {
                List<PhotoBucket> buckets = mModel.getBuckets();
                mView.openBucketList(buckets);
                mIsListOpen = true;
            } else {
                mView.closeBucketList(false);
                mIsListOpen = false;
            }
        }
    }

    public void selectBucket(PhotoBucket photoBucket, int position) {
        if (isViewAttached()) {
            UploadPhotoViewInterface mView = getView();
            mView.onSelectedBucket(position);
            mView.showPhotos(photoBucket.getPhotoSet());
            mView.closeBucketList(true);
            mIsListOpen = false;
        }
    }

    public void displayAllPhotos() {
        if (isViewAttached()) {
            UploadPhotoViewInterface mView = getView();
            Set<LocalPhoto> set = mModel.getBuckets().get(0).getPhotoSet();
            mView.showPhotos(set);
        }
    }

    public void openDetail(ArrayList<LocalPhoto> details, HashSet<Integer> selects, int index) {
        if (isViewAttached()) {
            UploadPhotoActivity activity = (UploadPhotoActivity) getView();
            Intent intent = new Intent(activity, LocalDetailActivity.class);
            intent.putExtra(DetailActivity.DETAILS_KEY, details);
            intent.putExtra(DetailActivity.CURRENT_INDEX, index);
            intent.putExtra(LocalDetailActivity.SELECT_SET, selects);
            activity.startActivityForResult(intent, OPEN_LOCAL_PHOTO_DETAIL);
        }
    }

    public void upLoadImage(List<File> selectedPhotos) {
        mModel.uploadImg(selectedPhotos, new OnUploadFinishListener() {
            @Override
            public void onUploadFinish(boolean success, String msg) {
                if (success) {
                    Message.obtain(mHandler, MSG_SUCCESS, "上传图片成功!").sendToTarget();
                } else {
                    Message.obtain(mHandler, MSG_FAIL, msg).sendToTarget();
                }
            }
        });
    }

    public boolean onBackPressed() {
        if (mIsListOpen && isViewAttached()) {
            UploadPhotoViewInterface mView = getView();
            mView.closeBucketList(false);
            mIsListOpen = false;
            return true;
        }
        return false;
    }

    public boolean cancelTask() {
        return mModel.cancel();
    }

}
