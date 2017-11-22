package com.example.zhaolexi.imageloader.presenter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.example.zhaolexi.imageloader.base.BasePresenter;
import com.example.zhaolexi.imageloader.model.ImageDetailModel;
import com.example.zhaolexi.imageloader.model.ImageDetailModelImpl;
import com.example.zhaolexi.imageloader.view.ImageDetailViewInterface;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public class ImageDetailPresenter extends BasePresenter<ImageDetailViewInterface> {

    private final int MSG_LOAD_SUCCESS=0;
    private final int MSG_LOAD_FAIL=1;
    private ImageDetailModel mModel;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(isViewAttached()) {
                ImageDetailViewInterface imageDetail=getView();
                switch (msg.what) {
                    case MSG_LOAD_SUCCESS:
                        imageDetail.showImage((Bitmap)msg.obj);
                        break;
                    case MSG_LOAD_FAIL:
                        imageDetail.showError("加载超时");
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        }
    };

    public ImageDetailPresenter(){
        mModel=new ImageDetailModelImpl(this);
    }

    public void loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) {
        mModel.loadBitmapFromDiskCache(url, reqWidth, reqHeight, new onLoadFinishListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                Message message = Message.obtain(mHandler, MSG_LOAD_SUCCESS, bitmap);
                message.sendToTarget();
            }

            @Override
            public void onFail(String reason) {
                mHandler.sendEmptyMessage(MSG_LOAD_FAIL);
            }
        });
    }

    public void loadFullImage(String url, ImageView imageView, int reqWidth, int reqHeight) {
        mModel.loadFullImg(url, imageView, reqWidth, reqHeight);
    }

    public interface onLoadFinishListener{
        void onSuccess(Bitmap bitmap);

        void onFail(String reason);
    }
}
