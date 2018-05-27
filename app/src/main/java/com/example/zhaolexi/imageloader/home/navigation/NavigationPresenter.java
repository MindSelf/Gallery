package com.example.zhaolexi.imageloader.home.navigation;

import android.app.Activity;
import android.content.Intent;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.me.info.UserInfoActivity;
import com.example.zhaolexi.imageloader.redirect.login.TokenManager;

public class NavigationPresenter extends BasePresenter<NavigationViewInterface, NavigationModel> {

    @Override
    protected NavigationModel newModel() {
        return new NavigationModelImpl();
    }

    public void preLoad() {

    }

    public void releaseData() {

    }

    public void openUserInfo() {
        if (isViewAttached() && TokenManager.isLogin()) {
            Activity activity = getView().getContactActivity();
            Intent intent = new Intent(activity, UserInfoActivity.class);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        }
    }
}
