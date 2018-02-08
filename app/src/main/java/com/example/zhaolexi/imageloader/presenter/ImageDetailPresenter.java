package com.example.zhaolexi.imageloader.presenter;

import android.graphics.Bitmap;
import android.os.Message;
import android.widget.ImageView;

import com.example.zhaolexi.imageloader.base.BasePresenter;
import com.example.zhaolexi.imageloader.model.ImageDetailModel;
import com.example.zhaolexi.imageloader.model.ImageDetailModelImpl;
import com.example.zhaolexi.imageloader.utils.loader.ImageLoader;
import com.example.zhaolexi.imageloader.view.ImageDetailViewInterface;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public class ImageDetailPresenter extends BasePresenter<ImageDetailViewInterface,ImageDetailModel> {

    @Override
    protected ImageDetailModel newModel() {
        return new ImageDetailModelImpl();
    }

    @Override
    protected void onMessageSuccess(Message msg) {
        ImageDetailViewInterface mView = getView();
        mView.showImage((Bitmap) msg.obj);
    }

    @Override
    protected void onMessageFail(Message msg) {
        ImageDetailViewInterface mView = getView();
        mView.showError((String) msg.obj);
    }

    public void loadBitmapFromDiskCache(String url, ImageLoader.TaskOptions options) {
        mModel.loadBitmapFromDiskCache(url, options, new com.example.zhaolexi.imageloader.callback.OnLoadFinishListener<Bitmap>() {
            @Override
            public void onSuccess(Bitmap data) {
                Message.obtain(mHandler, MSG_SUCCESS, data).sendToTarget();
            }

            @Override
            public void onFail(String reason) {
                Message.obtain(mHandler, MSG_FAIL, reason).sendToTarget();
            }
        });
    }

    public void loadFullImage(String url, ImageView imageView, ImageLoader.TaskOptions options) {
        mModel.loadFullImg(url, imageView, options);
    }

}
