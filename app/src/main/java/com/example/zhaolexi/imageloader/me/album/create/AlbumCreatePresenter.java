package com.example.zhaolexi.imageloader.me.album.create;

import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.common.utils.EncryptUtils;
import com.example.zhaolexi.imageloader.common.utils.SharePreferencesUtils;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.redirect.router.Result;

public class AlbumCreatePresenter extends BasePresenter<AlbumCreateViewInterface, AlbumCreateModel> {

    @Override
    protected AlbumCreateModel newModel() {
        return new AlbumCreateModelImpl();
    }

    @Override
    protected void onMessageSuccess(Message msg) {
        if (isViewAttached()) {
            getView().onCreateSuccess((Album) msg.obj);
        }
    }

    @Override
    protected void onMessageFail(Message msg) {
        if (isViewAttached()) {
            getView().onCreateFail((String) msg.obj);
        }
    }

    public void createAlbum(String title, String description, String readPassword, String modPassword) {
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(BaseApplication.getContext(), "相册名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkLength(readPassword) || !checkLength(modPassword)) {
            Toast.makeText(BaseApplication.getContext(), "密码长度必须在6~15位之间", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = String.format(Uri.CREATE_ALBUM, title, description, SharePreferencesUtils.getString(SharePreferencesUtils.USER_NAME, ""),
                EncryptUtils.digest(readPassword), EncryptUtils.digest(modPassword));
        mModel.createAlbum(url, new OnRequestFinishListener<Album>() {
            @Override
            public void onSuccess(Album data) {
                Message.obtain(mHandler, MSG_SUCCESS, data).sendToTarget();
            }

            @Override
            public void onFail(String reason, Result result) {
                Message.obtain(mHandler, MSG_FAIL, reason).sendToTarget();
            }
        });
    }

    private boolean checkLength(String password) {
        if (!TextUtils.isEmpty(password)) {
            int length = password.length();
            return length > 6 && length <= 15;
        }
        return true;
    }
}
