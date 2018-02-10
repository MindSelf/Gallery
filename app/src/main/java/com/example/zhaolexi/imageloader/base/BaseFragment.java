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

public abstract class BaseFragment<T extends BasePresenter> extends Fragment {

    protected T mPresenter;
    protected View contentView;
    private boolean isViewInflated;
    private boolean isVisibleToUser;

    /*
    与Activity建立关联，可以通过getActivity获取到关联的Activity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPresenter = createPresenter();
        initData();
        if (mPresenter != null) mPresenter.attachView(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
    正在加载视图，可以将加载出来的视图缓存下来，避免重复加载
    Note：加载视图时不要做耗时操作
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //当使用ViewPager时，container即为Viewpager
        //不管attachToRoot是否为null，ViewPager都会将contentView添加为其子View
        //如果将attachToRoot设置为true，ViewPager会将contentView的root（即为ViewPager本身）作为其子View，从而因为递归调用导致栈溢出
        if (contentView == null) {
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
        if (isVisibleToUser) {
            lazyLoad();
        }
        //当isVisibleToUser为false时，说明该Fragment被预加载出来还不对用户可见，这时先不要加载数据，而是
        //等到setUserVisibleHint(true)时再加载数据
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
    Activity的onCreate执行完毕,这时可以进行与activity有关的工作了
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
    销毁和回收Fragment
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*
    解除和Activity的关联
     */
    @Override
    public void onDetach() {
        super.onDetach();
        if (mPresenter != null) mPresenter.detachView();
    }

    /*
    是否对用户可见：
    第一次调用在onAttach前，Fragment正在初始化，返回false
    第二次在对用户可见时调用，返回true（如果这个Fragment不是预加载出来的，那么调用时机也在onAttach之前）
    最后一次调用在对用户不可见时，返回false
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (isVisibleToUser && isViewInflated) {
            //在对用户可见时isViewInflated为true，说明该Fragment已经预加载出来了
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
