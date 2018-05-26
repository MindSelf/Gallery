package com.example.zhaolexi.imageloader.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.redirect.router.Router;


/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public class PhotoDetailPresenter extends DetailPresenter<PhotoDetailViewInterface, PhotoDetailModel> {

    private static final int MODIFY = 2;
    private static final int DELETE = 3;
    private static final int DOWNLOAD = 4;

    @SuppressLint("HandlerLeak")
    private Handler mPhotoDetailHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isViewAttached()) {
                switch (msg.what) {
                    case MSG_SUCCESS:
                        getView().showHint((String) msg.obj);
                        switch (msg.arg1) {
                            case MODIFY:
                                getView().exitEditMode();
                                break;
                            case DELETE:
                                getView().deletePhoto();
                                break;
                            case DOWNLOAD:
                                break;
                        }
                        break;
                    case MSG_FAIL:
                        getView().showError((String) msg.obj);
                        break;
                }
            }
        }
    };

    @Override
    protected PhotoDetailModel newModel() {
        return new PhotoDetailModelImpl();
    }

    public void setAid(String aid) {
        mModel.setAid(aid);
    }

    public void modifyDescription(String pid, String desc) {
        mModel.modifyDescription(pid, desc, new OnRequestFinishListener() {
            @Override
            public void onSuccess(Object data) {
                Message message = Message.obtain(mPhotoDetailHandler, MSG_SUCCESS, "修改成功");
                message.arg1 = MODIFY;
                message.sendToTarget();
            }

            @Override
            public void onFail(String reason, Result result) {
                PhotoDetailPresenter.this.onFail(reason, result);
            }
        });
    }

    public void deletePhoto(String pid) {
        mModel.deletePhoto(pid, new OnRequestFinishListener() {
            @Override
            public void onSuccess(Object data) {
                Message message = Message.obtain(mPhotoDetailHandler, MSG_SUCCESS, "删除成功");
                message.arg1 = DELETE;
                message.sendToTarget();
            }

            @Override
            public void onFail(String reason, Result result) {
                PhotoDetailPresenter.this.onFail(reason, result);
            }
        });
    }

    private void onFail(String reason, Result result) {
        if (result != null) {
            if (isViewAttached()) {
                Activity activity = getView().getContactActivity();
                Router router = new Router.Builder(activity)
                        .setOriginAlbum(getView().getAlbumInfo())
                        .build();
                router.route(result);
            }
        } else if (!TextUtils.isEmpty(reason)) {
            Message.obtain(mPhotoDetailHandler, MSG_FAIL, reason).sendToTarget();
        }
    }

    public void toggleThumbUp(String pid) {
        mModel.toggleThumbUp(pid);
    }

    public void downloadToLocal(String detailUrl, String name) {
        mModel.downloadImg(detailUrl, name, new OnRequestFinishListener() {
            @Override
            public void onSuccess(Object data) {
                Message message = Message.obtain(mPhotoDetailHandler, MSG_SUCCESS, "保存图片成功");
                message.arg1 = DOWNLOAD;
                message.sendToTarget();
            }

            @Override
            public void onFail(String reason, Result result) {
                Message.obtain(mPhotoDetailHandler, MSG_FAIL, reason).sendToTarget();
            }
        });
    }
}
