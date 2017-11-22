package com.example.zhaolexi.imageloader.presenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.example.zhaolexi.imageloader.base.BasePresenter;
import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.bean.PhotoBucket;
import com.example.zhaolexi.imageloader.model.LocalPhotoModel;
import com.example.zhaolexi.imageloader.model.LocalPhotoModelImpl;
import com.example.zhaolexi.imageloader.view.ImageDetailActivity;
import com.example.zhaolexi.imageloader.view.SelectPhotoViewInterface;

import java.io.File;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/16.
 */

public class SeletePhotoPresenter extends BasePresenter<SelectPhotoViewInterface> {

    private LocalPhotoModel mModel;
    private boolean mIsListOpen;
    private static final int MSG_SUCCESS = 1;
    private static final int MSG_FAIL = -1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCCESS:
                    if (isViewAttached()) {
                        SelectPhotoViewInterface mView = getView();
                        mView.onUploadFinish(true, (String) msg.obj);
                    }
                    break;
                case MSG_FAIL:
                    if (isViewAttached()) {
                        SelectPhotoViewInterface mView = getView();
                        mView.onUploadFinish(false, (String) msg.obj);
                    }
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    public SeletePhotoPresenter() {
        mModel = new LocalPhotoModelImpl();
    }

    public void chooseBucketList() {
        if (isViewAttached()) {
            SelectPhotoViewInterface mView = getView();
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
            SelectPhotoViewInterface mView = getView();
            mView.changeSelectedBucket(position);
            mView.showPhotos(photoBucket.getPhotoList());
            mView.closeBucketList(true);
            mIsListOpen = false;
        }
    }

    public void displayAllPhotos() {
        if (isViewAttached()) {
            SelectPhotoViewInterface mView = getView();
            List<Photo> list = mModel.getBuckets().get(0).getPhotoList();
            mView.showPhotos(list);
        }
    }

    public void openDetail(String path) {
        if (isViewAttached()) {
            Activity activity = (Activity) getView();
            Intent intent = new Intent(activity, ImageDetailActivity.class);
            intent.putExtra("url", path);
            activity.startActivity(intent);
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
            SelectPhotoViewInterface mView = getView();
            mView.closeBucketList(false);
            mIsListOpen = false;
            return true;
        }
        return false;
    }

    public boolean cancleTask() {
        return mModel.cancle();
    }

    public interface OnUploadFinishListener {
        void onUploadFinish(boolean success, String msg);
    }
}
