package com.example.zhaolexi.imageloader.presenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.example.zhaolexi.imageloader.base.BasePresenter;
import com.example.zhaolexi.imageloader.bean.Image;
import com.example.zhaolexi.imageloader.model.ImageModel;
import com.example.zhaolexi.imageloader.model.ImageModelImpl;
import com.example.zhaolexi.imageloader.view.GalleryViewInterface;
import com.example.zhaolexi.imageloader.view.ImageDetailActivity;
import com.example.zhaolexi.imageloader.view.SelectPhotoActivity;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public class ImagePresenter extends BasePresenter<GalleryViewInterface> {

    private ImageModel mImageModel;
    private int currentPage;
    private boolean hasMoreData=true;

    private static final int LOAD_SUCCESS=1;
    private static final int LOAD_FAIL=2;

    //OkHttp是在子线程中执行回调方法的，所以要通过handler切换到主线程
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(isViewAttached()) {
                GalleryViewInterface mImageView=getView();
                switch (msg.what) {
                    case LOAD_SUCCESS:
                        List<Image> newDatas = (List<Image>) msg.obj;
                        mImageView.showNewDatas(hasMoreData,newDatas);
                        break;
                    case LOAD_FAIL:
                        mImageView.showError();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    };

    public ImagePresenter(){
        mImageModel=new ImageModelImpl(this);
    }

    public void loadMore(){
        if(isViewAttached()) {
            GalleryViewInterface mImageView=getView();
            if (hasMoreData) {
                mImageView.showLoading();
                mImageModel.loadUri(++currentPage, new OnLoadFinishListener() {
                    @Override
                    public void onLoadSuccess(boolean hasMore,List<Image> newData) {
                        hasMoreData=hasMore;
                        Message message = Message.obtain(mHandler, LOAD_SUCCESS, newData);
                        message.sendToTarget();
                    }

                    @Override
                    public void onLoadFail() {
                        Message message = Message.obtain(mHandler, LOAD_FAIL);
                        message.sendToTarget();
                    }
                });
            }
        }
    }

    public void setUrl(String newUrl) {
        mImageModel.setUri(newUrl);
    }

    public void resetState() {
        hasMoreData=true;
        currentPage=0;
    }

    public void openDetail(boolean hasFullImg,String url){
        Activity activity = (Activity) getView();
        Intent intent = new Intent(activity, ImageDetailActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("hasFullImg", hasFullImg);
        activity.startActivity(intent);
    }

    public void selectPhoto() {
        Activity activity=(Activity) getView();
        Intent intent = new Intent(activity, SelectPhotoActivity.class);
        activity.startActivity(intent);
    }



    public interface OnLoadFinishListener{
        void onLoadSuccess(boolean hasMore,List<Image> newData);

        void onLoadFail();
    }
}
