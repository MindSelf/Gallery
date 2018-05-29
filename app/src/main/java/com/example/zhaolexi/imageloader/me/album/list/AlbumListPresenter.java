package com.example.zhaolexi.imageloader.me.album.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;

import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.me.album.info.AlbumInfoActivity;
import com.example.zhaolexi.imageloader.me.album.list.favorite.FavoriteManager;
import com.example.zhaolexi.imageloader.redirect.router.Result;

import java.util.List;

public class AlbumListPresenter extends BasePresenter<AlbumListViewInterface, AlbumListModel> {

    private static final int REFRESH_FINISH = 1;

    private int mCurPage = 1;
    private boolean mHasMoreData = true;

    @Override
    protected AlbumListModel newModel() {
        return null;
    }

    public void setModel(AlbumListModel model) {
        mModel = model;
    }

    @Override
    protected void onMessageSuccess(Message msg) {
        if (isViewAttached()) {
            AlbumListViewInterface albumList = getView();
            //刷新成功，更新适配器中的数据
            if (msg.arg1 == REFRESH_FINISH) {
                albumList.onRefreshFinish();
            }
            List<Album> newData = (List<Album>) msg.obj;
            albumList.showNewData(newData);
            mCurPage++;
        }
    }

    @Override
    protected void onMessageFail(Message msg) {
        if (isViewAttached()) {
            AlbumListViewInterface albumList = getView();
            //刷新失败，保留原始数据，并显示错误信息
            albumList.showError((String) msg.obj);
        }
    }

    public void loadMore() {
        if (mHasMoreData) {
            mModel.loadImage(mCurPage + 1, new OnRequestFinishListener<List<Album>>() {
                @Override
                public void onSuccess(List<Album> newData) {
                    mHasMoreData = newData.size() >= 12;
                    Message.obtain(mHandler, MSG_SUCCESS, newData).sendToTarget();
                }

                @Override
                public void onFail(String reason, Result result) {
                    Message.obtain(mHandler, MSG_FAIL, reason).sendToTarget();
                }
            });

        }
    }

    public void refresh() {

        mHasMoreData = true;
        mCurPage = 0;

        mModel.loadImage(mCurPage + 1, new OnRequestFinishListener<List<Album>>() {
            @Override
            public void onSuccess(List<Album> newData) {
                mHasMoreData = newData.size() >= 12;
                Message message = Message.obtain(mHandler, MSG_SUCCESS, newData);
                message.arg1 = REFRESH_FINISH;
                message.sendToTarget();
            }

            @Override
            public void onFail(String reason, Result result) {
                Message message = Message.obtain(mHandler, MSG_FAIL);
                message.arg1 = REFRESH_FINISH;
                message.sendToTarget();
            }
        });
    }

    public void openAlbumInfo(Album album, int type) {
        if (isViewAttached()) {
            Activity activity = getView().getContactActivity();
            Intent intent = new Intent(activity, AlbumInfoActivity.class);
            intent.putExtra(AlbumInfoActivity.ALBUM, album);
            intent.putExtra(AlbumInfoActivity.MODIFY_MODE, type == AlbumListActivity.TYPE_MY);
            activity.startActivityForResult(intent, AlbumListActivity.REQUEST_INFO);
        }
    }

    public void closeAlbum(final Album album, final int pos, final int type) {
        if (isViewAttached()) {
            mModel.closeAlbum(album.getAid(), new OnRequestFinishListener() {
                @Override
                public void onSuccess(Object data) {
                    Activity activity = getView().getContactActivity();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getView().onCloseSuccess(pos, type);
                            if (type == AlbumListActivity.TYPE_FAVORITE) {
                                FavoriteManager.notifyCollectionCancel(album.getAid());
                            }
                        }
                    });
                }

                @Override
                public void onFail(final String reason, Result result) {
                    Activity activity = getView().getContactActivity();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getView().onCloseFail(reason);
                        }
                    });
                }
            });
        }
    }
}
