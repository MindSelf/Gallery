package com.example.zhaolexi.imageloader.home.navigation;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;

public interface NavigationViewInterface extends BaseViewInterface<NavigationPresenter>{

    void attachPresenter();

    void detachPresenter();

    void onNavigationShown();

    void onNavigationDismiss();

    void onLoginStateChange();
}
