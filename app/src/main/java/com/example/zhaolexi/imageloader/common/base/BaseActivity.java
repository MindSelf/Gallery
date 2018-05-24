package com.example.zhaolexi.imageloader.common.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity<T extends BasePresenter> extends AppCompatActivity {

    protected T mPresenter;

    /*
    在onCreate中对数据和视图进行初始化操作
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化Presenter并与其绑定
        mPresenter = createPresenter();
        //加载数据
        initData();
        //初始化视图
        initView();
        if(mPresenter!=null) mPresenter.attachView(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPresenter!=null) mPresenter.detachView();
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
