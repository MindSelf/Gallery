package com.example.zhaolexi.imageloader.presenter;

import android.os.Message;

import com.example.zhaolexi.imageloader.adapter.AlbumPagerAdapter;
import com.example.zhaolexi.imageloader.adapter.ManagedAlbumAdapter;
import com.example.zhaolexi.imageloader.base.BasePresenter;
import com.example.zhaolexi.imageloader.bean.Album;
import com.example.zhaolexi.imageloader.callback.OnLoadFinishListener;
import com.example.zhaolexi.imageloader.model.GalleryModel;
import com.example.zhaolexi.imageloader.model.GalleryModelImpl;
import com.example.zhaolexi.imageloader.view.GalleryViewInterface;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/2/5.
 */

public class GalleryPresenter extends BasePresenter<GalleryViewInterface, GalleryModel> {

    private int mCurrentPage;
//    private boolean mHasDataSetChanged;

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

//    public boolean hasDataSetChanged() {
//        return mHasDataSetChanged;
//    }

//    public void clearDataSetChangedState() {
//        mHasDataSetChanged=false;
//    }

    public List<Album> getLocalHistory() {
        return mModel.loadLocalHistory();
    }

    public void getRandom() {
        mModel.getRandom(new OnLoadFinishListener<List<Album>>() {
            @Override
            public void onSuccess(List<Album> data) {
                Message.obtain(mHandler, MSG_SUCCESS, data).sendToTarget();
            }

            @Override
            public void onFail(String reason) {
                Message.obtain(mHandler, MSG_FAIL, reason).sendToTarget();
            }
        });
    }

    public void addAlbum(Album album) {
        if (isViewAttached()) {
            GalleryViewInterface mView = getView();
            ManagedAlbumAdapter albumAdapter = mView.getAlbumAdapter();
            AlbumPagerAdapter pagerAdapter=mView.getPagerAdapter();
            //如果该相册在本地相册中不存在，将其添加到本地相册中
            if (albumAdapter.getLocalIndexOfAlbum(album) < 0) {
                albumAdapter.addAlbumToLocal(album);
                mModel.addAlbumToDB(album);
                pagerAdapter.notifyDataSetChanged();
//                mHasDataSetChanged = true;
            }
            mCurrentPage = albumAdapter.getLocalIndexOfAlbum(album);
            notifyCurrentPageChanged();
        }
    }

    public void removeAlbum(int position) {
        if (isViewAttached()) {
            GalleryViewInterface mView = getView();
            ManagedAlbumAdapter albumAdapter = mView.getAlbumAdapter();
            AlbumPagerAdapter pagerAdapter=mView.getPagerAdapter();
            mModel.removeAlbumFromDB(albumAdapter.getAlbum(position));
            albumAdapter.removeAlbum(position);
            if (position == mCurrentPage) {
                mCurrentPage = 0;
                notifyCurrentPageChanged();
            }
//            mHasDataSetChanged = true;
            pagerAdapter.notifyDataSetChanged();
        }
    }

    public void moveAlbum(int from, int to) {
        if (from == mCurrentPage) {
            mCurrentPage = to;
        } else if (to == mCurrentPage) {
            mCurrentPage=from;
        }
        notifyCurrentPageChanged();

        if (isViewAttached()) {
            AlbumPagerAdapter pagerAdapter=getView().getPagerAdapter();
            pagerAdapter.notifyDataSetChanged();
//        mHasDataSetChanged = true;
        }
    }

    private void notifyCurrentPageChanged() {
        //最好改成观察者模式
        if (isViewAttached()) {
            ManagedAlbumAdapter albumAdapter = getView().getAlbumAdapter();
            albumAdapter.notifyDataSetChanged();
        }
    }
}
