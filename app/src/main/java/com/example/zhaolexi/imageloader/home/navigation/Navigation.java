package com.example.zhaolexi.imageloader.home.navigation;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.utils.SharePreferencesUtils;
import com.example.zhaolexi.imageloader.redirect.login.OnLoginStateChangeCallback;
import com.example.zhaolexi.imageloader.redirect.login.TokenManager;

import java.lang.ref.WeakReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class Navigation implements NavigationViewInterface, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private NavigationPresenter mPresenter;
    private DefaultLoginStateCallback mCallback;
    private NavigationView mNavigation;
    private DrawerLayout mDrawerLayout;
    private CircleImageView mFigure;
    private TextView mName;

    public Navigation(DrawerLayout drawerLayout, NavigationView navigationView) {
        mCallback = new DefaultLoginStateCallback(this);
        TokenManager.addLoginStateChangeCallback(mCallback);
        mPresenter = new NavigationPresenter();
        initView(drawerLayout, navigationView);
    }

    private void initView(DrawerLayout drawerLayout, NavigationView navigationView) {
        mDrawerLayout = drawerLayout;
        mNavigation = navigationView;
        mNavigation.setNavigationItemSelectedListener(this);
        mNavigation.getMenu().setGroupVisible(R.id.group_navigation, TokenManager.isLogin());

        ViewGroup header = (ViewGroup) mNavigation.getHeaderView(0);
        mFigure = (CircleImageView) header.findViewById(R.id.iv_figure);
        mFigure.setImageResource(TokenManager.isLogin() ? R.mipmap.ic_user_login : R.mipmap.ic_user_unlogin);
        mFigure.setOnClickListener(this);

        mName = (TextView) header.findViewById(R.id.tv_name);
        mName.setText(SharePreferencesUtils.getString(SharePreferencesUtils.USER_NAME, ""));
    }

    @Override
    public void attachPresenter() {
        mPresenter.attachView(this);
    }

    @Override
    public void detachPresenter() {
        mPresenter.detachView();
    }

    @Override
    public void onNavigationShown() {
        getPresenter().preLoad();
    }

    @Override
    public void onNavigationDismiss() {
        getPresenter().releaseData();
    }

    @Override
    public void onLoginStateChange() {
        mNavigation.getMenu().setGroupVisible(R.id.group_navigation, TokenManager.isLogin());
        mFigure.setImageResource(TokenManager.isLogin() ? R.mipmap.ic_user_login : R.mipmap.ic_user_unlogin);
        mName.setText(SharePreferencesUtils.getString(SharePreferencesUtils.USER_NAME, ""));
    }

    @Override
    public Activity getContactActivity() {
        return (Activity) mNavigation.getContext();
    }

    @Override
    public NavigationPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.my_album:
                mPresenter.openMyAlbum();
                break;
            case R.id.my_collection:
                mPresenter.openFavoriteAlbum();
                break;
        }
        mDrawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_figure:
                mPresenter.openUserInfo();
                mDrawerLayout.closeDrawers();
                break;
        }
    }

    private static class DefaultLoginStateCallback implements OnLoginStateChangeCallback {

        private WeakReference<NavigationViewInterface> mRef;

        DefaultLoginStateCallback(NavigationViewInterface navigation) {
            mRef = new WeakReference<>(navigation);
        }

        @Override
        public void onSignIn() {
            if (mRef.get() != null) {
                mRef.get().onLoginStateChange();
            }
        }

        @Override
        public void onLogOut() {
            if (mRef.get() != null) {
                mRef.get().onLoginStateChange();
            }
        }
    }
}
