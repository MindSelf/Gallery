package com.example.zhaolexi.imageloader.base;

import android.app.Activity;
import android.os.Bundle;

public abstract class BaseActivity<V,T extends BasePresenter> extends Activity {

    protected T mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        mPresenter=createPresenter();
        mPresenter.attachView((V)this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    protected abstract void initView();

    protected abstract T createPresenter();
}
