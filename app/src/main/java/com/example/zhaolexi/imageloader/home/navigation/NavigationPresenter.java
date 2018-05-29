package com.example.zhaolexi.imageloader.home.navigation;

import android.app.Activity;
import android.content.Intent;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.me.album.list.AlbumListActivity;
import com.example.zhaolexi.imageloader.me.info.UserInfoActivity;
import com.example.zhaolexi.imageloader.redirect.login.LoginActivity;
import com.example.zhaolexi.imageloader.redirect.login.TokenManager;

import java.util.ArrayList;

public class NavigationPresenter extends BasePresenter<NavigationViewInterface, NavigationModel> {

    @Override
    protected NavigationModel newModel() {
        return new NavigationModelImpl();
    }

    public void preLoad() {
        mModel.loadAlbumInfo();
    }

    public void releaseData() {
        mModel.releaseAlbumInfo();
    }

    public void openUserInfo() {
        if (isViewAttached()) {
            Activity activity = getView().getContactActivity();
            if (TokenManager.isLogin()) {
                Intent intent = new Intent(activity, UserInfoActivity.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
            } else {
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
            }
        }
    }

    public void openMyAlbum() {
        if (isViewAttached()) {
            Intent intent = new Intent(getView().getContactActivity(), AlbumListActivity.class);
            intent.putExtra(AlbumListActivity.KEY_TYPE, AlbumListActivity.TYPE_MY);
            intent.putExtra(AlbumListActivity.KEY_ALBUM, (ArrayList) mModel.getMyAlbums());
            getView().getContactActivity().startActivity(intent);
        }
    }

    public void openFavoriteAlbum() {
        if (isViewAttached()) {
            Intent intent = new Intent(getView().getContactActivity(), AlbumListActivity.class);
            intent.putExtra(AlbumListActivity.KEY_TYPE, AlbumListActivity.TYPE_FAVORITE);
            intent.putExtra(AlbumListActivity.KEY_ALBUM, (ArrayList) mModel.getFavoriteAlbums());
            getView().getContactActivity().startActivity(intent);
        }
    }
}
