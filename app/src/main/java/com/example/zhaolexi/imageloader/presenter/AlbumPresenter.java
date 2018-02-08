package com.example.zhaolexi.imageloader.presenter;

import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;

import com.example.zhaolexi.imageloader.base.BasePresenter;
import com.example.zhaolexi.imageloader.bean.Album;
import com.example.zhaolexi.imageloader.bean.Image;
import com.example.zhaolexi.imageloader.model.AlbumModel;
import com.example.zhaolexi.imageloader.model.AlbumModelImpl;
import com.example.zhaolexi.imageloader.utils.MyUtils;
import com.example.zhaolexi.imageloader.view.AlbumFragment;
import com.example.zhaolexi.imageloader.view.AlbumViewInterface;
import com.example.zhaolexi.imageloader.view.GalleryActivity;
import com.example.zhaolexi.imageloader.view.ImageDetailActivity;
import com.example.zhaolexi.imageloader.view.UploadPhotoActivity;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public class AlbumPresenter extends BasePresenter<AlbumViewInterface,AlbumModel> {

    private int currentPage;
    private boolean hasMoreData = true;

    private static final int REFRESH_FINISH = 2;
    public static final int SELECT_PHOTO = 3;

    @Override
    protected AlbumModel newModel() {
        return new AlbumModelImpl();
    }

    @Override
    public void attachView(AlbumViewInterface view) {
        super.attachView(view);
        mModel.setUrl(view.getAlbumInfo().getUrl());
    }

    @Override
    protected void onMessageSuccess(Message msg) {
        AlbumViewInterface mView=getView();
        mView.setRefreshing(false);
        //刷新成功，更新适配器中的数据
        if (msg.arg1 == REFRESH_FINISH) {
            currentPage = 1;
            mView.getAdapter().cleanImages();
        }
        List<Image> newData = (List<Image>) msg.obj;
        mView.showNewData(hasMoreData, newData);
        currentPage++;
    }

    @Override
    protected void onMessageFail(Message msg) {
        AlbumViewInterface mView=getView();
        mView.setRefreshing(false);
        //刷新失败，保留原始数据，并显示错误信息
        mView.showError();
    }

    public void loadMore() {
        if (isViewAttached()) {
            AlbumViewInterface albumView = getView();
            if (hasMoreData) {
                if (currentPage == 0)
                    albumView.setRefreshing(true);
                else
                    albumView.showLoading();
                mModel.loadImage(currentPage + 1, new OnLoadFinishListener() {
                    @Override
                    public void onLoadSuccess(boolean hasMore, List<Image> newData) {
                        hasMoreData = hasMore;
                        Message message = Message.obtain(mHandler, MSG_SUCCESS, newData);
                        message.sendToTarget();
                    }

                    @Override
                    public void onLoadFail() {
                        Message message = Message.obtain(mHandler, MSG_FAIL);
                        message.sendToTarget();
                    }
                });
            }
        }
    }

    public void refresh() {

        if (isViewAttached()) {

            AlbumFragment fragment = (AlbumFragment) getView();
            GalleryActivity activity=(GalleryActivity) fragment.getActivity();
            //如果wifi可用或者已同意使用流量，更新图片，否则将会弹出警告
            if (!activity.mCanLoadWithoutWifi && !MyUtils.isWifiAvailable(activity)) {
                getView().showAlertDialog();
                return;
            }

            getView().setRefreshing(true);
            hasMoreData = true;

            mModel.loadImage(1, new OnLoadFinishListener() {
                @Override
                public void onLoadSuccess(boolean hasMore, List<Image> newData) {
                    hasMoreData = hasMore;
                    //OkHttp是在子线程中执行回调方法的，所以要通过handler切换到主线程
                    Message message = Message.obtain(mHandler, MSG_SUCCESS, newData);
                    message.arg1 = REFRESH_FINISH;
                    message.sendToTarget();
                }

                @Override
                public void onLoadFail() {
                    Message message = Message.obtain(mHandler, MSG_FAIL);
                    message.arg1 = REFRESH_FINISH;
                    message.sendToTarget();
                }
            });
        }
    }

    public void openDetail(boolean hasFullImg, String url) {
        if (isViewAttached()) {
            AlbumFragment fragment = (AlbumFragment) getView();
            Intent intent = new Intent(fragment.getContext(), ImageDetailActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("hasFullImg", hasFullImg);
            fragment.startActivity(intent);
        }
    }

    public void addPhoto() {
        if (isViewAttached()) {
            AlbumFragment fragment = (AlbumFragment) getView();
            Album albumInfo = fragment.getAlbumInfo();
            if (albumInfo.isAccessible() && !TextUtils.isEmpty(albumInfo.getAid())) {
                Intent intent = new Intent(fragment.getContext(), UploadPhotoActivity.class);
                intent.putExtra(AlbumFragment.KEY_AID, albumInfo.getAid());
                fragment.startActivityForResult(intent, SELECT_PHOTO);
            }
        }
    }

    public interface OnLoadFinishListener {
        void onLoadSuccess(boolean hasMore, List<Image> newData);

        void onLoadFail();
    }
}
