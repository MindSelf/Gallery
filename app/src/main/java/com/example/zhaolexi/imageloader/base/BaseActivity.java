package com.example.zhaolexi.imageloader.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity<V,T extends BasePresenter> extends AppCompatActivity {

    protected T mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
        mPresenter=createPresenter();
        mPresenter.attachView((V)this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        //在退出activity时不会立刻gc，要是反复启动activity，会导致OOM
        System.gc();
    }

    public T getPresenter() {
        return mPresenter;
    }

    protected abstract void initData();

    protected abstract void initView();

    protected abstract T createPresenter();
}
