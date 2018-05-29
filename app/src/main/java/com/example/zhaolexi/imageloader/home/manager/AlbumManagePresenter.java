package com.example.zhaolexi.imageloader.home.manager;

import android.app.Activity;
import android.os.Message;

import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.utils.AlbumConstructor;
import com.example.zhaolexi.imageloader.home.gallery.AlbumPagerAdapter;
import com.example.zhaolexi.imageloader.redirect.router.RedirectCallback;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.example.zhaolexi.imageloader.redirect.router.Router;

import java.util.List;

public class AlbumManagePresenter extends BasePresenter<AlbumManageViewInterface, AlbumManageModel> {

    private int mCurrentItem;

    public int getCurrentPage() {
        return mCurrentItem;
    }

    public void setCurrentPage(int currentPage) {
        this.mCurrentItem = currentPage;
    }

    private boolean mShouldUpdateState;

    public boolean shouldUpdateState() {
        return mShouldUpdateState;
    }

    public void clearUpdateState() {
        mShouldUpdateState = false;
    }

    @Override
    protected AlbumManageModel newModel() {
        return new AlbumManageModelImpl();
    }

    @Override
    protected void onMessageSuccess(Message msg) {
        AlbumManageViewInterface mView = getView();
        mView.showRandom((List<Album>) msg.obj);
    }

    @Override
    protected void onMessageFail(Message msg) {
        AlbumManageViewInterface mView = getView();
        mView.showError((String) msg.obj);
    }


    public void getRandom() {
        mModel.getRandom(new OnRequestFinishListener<List<Album>>() {
            @Override
            public void onSuccess(List<Album> data) {
                Message.obtain(mHandler, MSG_SUCCESS, data).sendToTarget();
            }

            @Override
            public void onFail(String reason, Result result) {
                if (result != null && isViewAttached()) {
                    final Activity activity = getView().getContactActivity();
                    Router router = new Router.Builder(activity)
                            .setLoginCallback(new RedirectCallback() {
                                @Override
                                protected void onCallback(boolean success) {
                                    if (success) {
                                        getRandom();
                                    }
                                }
                            })
                            .build();
                    if (router.route(result)) {
                        Message.obtain(mHandler, MSG_FAIL, reason).sendToTarget();
                    }
                }
            }
        });
    }

    //The application's PagerAdapter changed the adapter's contents should call PagerAdapter#notifyDataSetChanged!
    public void addAlbum(Album album) {
        if (isViewAttached()) {
            AlbumManageViewInterface mView = getView();
            ManagedAlbumAdapter albumAdapter = mView.getAlbumAdapter();
            AlbumPagerAdapter pagerAdapter = mView.getPagerAdapter();
            //construct album
            new AlbumConstructor().construct(album);

            int index = albumAdapter.getIndexOfLocalAlbum(album);
            if (index < 0) {
                //如果该相册在本地相册中不存在，将其添加到本地相册中
                albumAdapter.addAlbumToLocal(album);
                mModel.addAlbumToDB(album);
                pagerAdapter.notifyDataSetChanged();
            } else {
                //如果相册权限发生改变，更新本地相册
                Album origin = albumAdapter.getLocalAlbum(index);
                if (origin.isAccessible() != album.isAccessible()) {
                    origin.setAccessible(album.isAccessible());
                    origin.save();
                    pagerAdapter.notifyDataSetChanged();
                }
            }
            mCurrentItem = albumAdapter.getIndexOfLocalAlbum(album);
            notifyCurrentPageChanged();
        }
    }

    public void removeAlbum(Album album) {
        if (isViewAttached()) {
            ManagedAlbumAdapter albumAdapter = getView().getAlbumAdapter();
            int pos = albumAdapter.getIndexOfLocalAlbum(album);
            removeAlbum(pos);
        }
    }

    public void removeAlbum(int position) {
        if (isViewAttached()) {
            AlbumManageViewInterface mView = getView();
            ManagedAlbumAdapter albumAdapter = mView.getAlbumAdapter();
            AlbumPagerAdapter pagerAdapter = mView.getPagerAdapter();
            Album remove = albumAdapter.removeAlbum(position);
            if (remove != null) {
                mModel.removeAlbumFromDB(remove);
            }
            if (position >= 0) {
                if (position == mCurrentItem) {
                    mCurrentItem = 0;
                    notifyCurrentPageChanged();
                } else if (position < mCurrentItem) {
                    mCurrentItem--;
                    notifyCurrentPageChanged();
                }
                pagerAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onAlbumMove(int from, int to) {
        if (from == mCurrentItem) {
            mCurrentItem = to;
        } else if (to == mCurrentItem) {
            mCurrentItem = from;
        }
        //onAlbumMove在item移动时调用，如果此时更新UI，会主线程阻塞并结束item的移动
        //所以等到edit结束后才更新UI
        mShouldUpdateState = true;
    }

    private void notifyCurrentPageChanged() {
        //最好改成观察者模式
        if (isViewAttached()) {
            ManagedAlbumAdapter albumAdapter = getView().getAlbumAdapter();
            albumAdapter.notifyDataSetChanged();
        }
    }
}
