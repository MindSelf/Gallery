package com.example.zhaolexi.imageloader.detail;

import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;

import java.util.List;

public abstract class DetailPresenter<V extends DetailViewInterface, M extends DetailModel> extends BasePresenter<V, M> {

    private static final String TAG = "DetailPresenter";
    private static final int IS_RETRY = 1;

    private boolean hasMoreData = true;
    private int currentPage;


    @Override
    protected void onMessageSuccess(Message msg) {
        if (isViewAttached()) {
            DetailViewInterface mView = getView();
            List<Detail> newData = (List<Detail>) msg.obj;
            boolean isRetry = msg.arg1 > 0;
            if (isRetry) {
                Log.d(TAG, "retry: success");
            }
            mView.showNewData(newData, isRetry);
        }
    }

    @Override
    protected void onMessageFail(Message msg) {
        if (isViewAttached()) {
            DetailViewInterface mView = getView();
            String reason = (String) msg.obj;
            boolean isRetry = msg.arg1 > 0;
            Log.d(TAG, "load fail，should show error：" + isRetry);
            //如果是预加载出错，则不显示错误信息
            if (isRetry) {
                mView.showError(reason);
            }
        }
    }

    public void loadMoreData(final boolean isRetry) {

        if (hasMoreData) {
            if (!isRetry) {
                Log.d(TAG, "loadMoreData: preLoad");
            } else {
                Log.d(TAG, "loadMoreData: retry request");
            }

            mModel.loadMoreData(currentPage + 1, new OnRequestFinishListener<List<Detail>>() {
                @Override
                public void onSuccess(List<Detail> newData) {
                    hasMoreData = newData.size() >= 10;
                    currentPage++;
                    Message message = Message.obtain(mHandler, MSG_SUCCESS, newData);
                    message.arg1 = isRetry ? IS_RETRY : 0;
                    message.sendToTarget();
                }

                @Override
                public void onFail(String reason, Result result) {
                    Message message = Message.obtain(mHandler, MSG_FAIL, reason);
                    message.arg1 = isRetry ? IS_RETRY : 0;
                    message.sendToTarget();
                }
            });
        }
    }

    public void onOverScroll() {
        if (!hasMoreData) {
            if (isViewAttached()) {
                DetailViewInterface mView = getView();
                mView.showNoMoreData("没有更多图片了");
            }
        } else {
            loadMoreData(true);
        }
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setUrl(String url) {
        mModel.setUrl(url);
    }

    public void finish(Intent intent) {
        intent.putExtra(DetailActivity.CURRENT_PAGE, currentPage);
    }
}
