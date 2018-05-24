package com.example.zhaolexi.imageloader.home.gallery;

import android.app.Activity;
import android.os.Message;

import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.common.global.Result;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.utils.AlbumConstructor;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.home.manager.ManagedAlbumAdapter;
import com.example.zhaolexi.imageloader.redirect.router.RedirectCallback;
import com.example.zhaolexi.imageloader.redirect.router.Router;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/2/5.
 */

public class GalleryPresenter extends BasePresenter<GalleryViewInterface, GalleryModel> {

    private int mCurrentPage;
    private boolean mShouldUpdateState;

    @Override
    protected GalleryModel newModel() {
        return new GalleryModelImpl();
    }

    @Override
    protected void onMessageSuccess(Message msg) {
        GalleryViewInterface mView = getView();
        mView.showRandom((List<Album>) msg.obj);
    }

    @Override
    protected void onMessageFail(Message msg) {
        GalleryViewInterface mView = getView();
        mView.showError((String) msg.obj);
    }


    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.mCurrentPage = currentPage;
    }


    public boolean shouldUpdateState() {
        return mShouldUpdateState;
    }

    public void clearUpdateState() {
        mShouldUpdateState = false;
    }


    public List<Album> getLocalHistory() {
        return mModel.loadLocalHistory();
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
            GalleryViewInterface mView = getView();
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
            }else{
                //如果相册权限发生改变，更新本地相册
                Album origin = albumAdapter.getLocalAlbum(index);
                if (origin.isAccessible() != album.isAccessible()) {
                    origin.setAccessible(album.isAccessible());
                    origin.save();
                    pagerAdapter.notifyDataSetChanged();
                }
            }
            mCurrentPage = albumAdapter.getIndexOfLocalAlbum(album);
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
            GalleryViewInterface mView = getView();
            ManagedAlbumAdapter albumAdapter = mView.getAlbumAdapter();
            AlbumPagerAdapter pagerAdapter = mView.getPagerAdapter();
            mModel.removeAlbumFromDB(albumAdapter.getAlbum(position));
            albumAdapter.removeAlbum(position);
            if (position == mCurrentPage) {
                mCurrentPage = 0;
                notifyCurrentPageChanged();
            } else if (position < mCurrentPage) {
                mCurrentPage--;
                notifyCurrentPageChanged();
            }
            pagerAdapter.notifyDataSetChanged();
        }
    }

    public void onAlbumMove(int from, int to) {
        if (from == mCurrentPage) {
            mCurrentPage = to;
        } else if (to == mCurrentPage) {
            mCurrentPage = from;
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
