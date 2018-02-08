package com.example.zhaolexi.imageloader.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ZHAOLEXI on 2018/1/25.
 */

public abstract class BaseFragment <T extends BasePresenter>  extends Fragment {

    protected T mPresenter;
    protected View contentView;
    private boolean isViewInflated;
    private boolean isVisibleToUser;

    /*
    与Activity绑定，在这里可以获取到Activity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPresenter=createPresenter();
        initData();
        if (mPresenter!=null) mPresenter.attachView(this);
    }

    /*
    Fragment和SaveInstancedState被创建，可以通过savedInstanceState恢复数据
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
    加载视图，可以将加载出来的视图缓存下来，避免重复加载
    Note：加载视图时不要做耗时操作
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //当使用ViewPager时，container即为Viewpager
        //不管attachToRoot是否为null，ViewPager都会将contentView添加为其子View
        //如果将attachToRoot设置为true，ViewPager会将contentView的root（即为ViewPager本身）作为其子View，从而因为递归调用导致栈溢出
        if(contentView==null) {
            contentView = inflater.inflate(getResId(), container, false);
        }
        return contentView;
    }

    /*
    视图被加载完成后回调，可以进行View的初始化
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView(view);
        isViewInflated = true;
        //在onViewCreated前用户已经可见，表示当前Fragment没经过预加载而是立即显示出来，需要在这里进行懒加载
        if (isVisibleToUser) {
            lazyLoad();
        }
    }

    /*
    Activity的onCreate执行完毕,这时可以进行与activity有关的初始化工作了
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /*
    销毁视图
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /*
    被销毁和回收
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*
    和Activity解绑
     */
    @Override
    public void onDetach() {
        super.onDetach();
        if(mPresenter!=null) mPresenter.detachView();
    }

    /*
    是否对用户可见，这个回调函数在attach前会被调用2次（false），在onCreateView前被调用1次
    如果当前Fragment是被预加载的，那么在onCreateView前isVisibleToUser为false
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser=isVisibleToUser;
        if (isVisibleToUser && isViewInflated) {
            //一般来说，当调用setUserVisibleHint时isViewInflated仍为false
            //这里表示该Fragment已经过预加载，当被用户切换时，需要进行懒加载
            lazyLoad();
        }
    }

    public T getPresenter() {
        return mPresenter;
    }

    protected abstract T createPresenter();

    protected abstract int getResId();

    protected abstract void lazyLoad();

    protected abstract void initData();

    protected abstract void initView(View contentView);
}
