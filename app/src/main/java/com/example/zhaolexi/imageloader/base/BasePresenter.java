package com.example.zhaolexi.imageloader.base;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

/*
将与Model交互的逻辑抽象出来
 */
public abstract class BasePresenter<V,M> {

    protected static final int MSG_FAIL = 0;
    protected static final int MSG_SUCCESS = 1;
    protected BaseHandler mHandler;
    protected M mModel;
    private Reference<V> mViewRef;

    public BasePresenter() {
        mHandler = new BaseHandler(this);
        mModel = newModel();
    }

    protected V getView() {
        return mViewRef.get();
    }

    protected boolean isViewAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }

    public void attachView(V view) {
        mViewRef = new WeakReference<>(view);
    }

    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }

    protected abstract M newModel();

    //如果Message在MessageQueue中未被处理，Handler会被Message引用
    //如果不使用静态内部类，Handler会持有外部类的引用，最终导致Activity无法及时释放
    protected static class BaseHandler extends Handler {

        private SoftReference<BasePresenter> mPresenter;

        BaseHandler(BasePresenter presenter) {
            mPresenter = new SoftReference<>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mPresenter.get() != null) {
                BasePresenter presenter = mPresenter.get();
                if (presenter.isViewAttached()) {
                    switch (msg.what) {
                        case MSG_SUCCESS:
                            presenter.onMessageSuccess(msg);
                            break;
                        case MSG_FAIL:
                            presenter.onMessageFail(msg);
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }
            }
        }
    }

    protected void onMessageSuccess(Message msg) {}

    protected void onMessageFail(Message msg) {}
}
