package com.example.zhaolexi.imageloader.me.album.info;

import android.content.Intent;
import android.os.Message;

import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.common.utils.EncryptUtils;
import com.example.zhaolexi.imageloader.home.gallery.GalleryActivity;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.me.album.info.modify.ModifyInfoActivity;
import com.example.zhaolexi.imageloader.redirect.router.Result;

public class AlbumInfoPresenter extends BasePresenter<AlbumInfoViewInterface, AlbumInfoModel> {
    @Override
    protected AlbumInfoModel newModel() {
        return new AlbumInfoModelImpl();
    }

    @Override
    protected void onMessageSuccess(Message msg) {
        if (isViewAttached()) {
            getView().onSwitchToPublicSuccess();
        }
    }

    @Override
    protected void onMessageFail(Message msg) {
        if (isViewAttached()) {
            getView().onSwitchToPublicFail((String) msg.obj);
        }
    }

    public void openModifyInfo(Album album, int type) {
        if (isViewAttached()) {
            Intent intent = new Intent(getView().getContactActivity(), ModifyInfoActivity.class);
            intent.putExtra(ModifyInfoActivity.KEY_TYPE, type);
            intent.putExtra(ModifyInfoActivity.KEY_ALBUM, album);
            getView().getContactActivity().startActivityForResult(intent, AlbumInfoActivity.REQUEST_MODIFY);
        }
    }

    public void visitAlbum(Album album) {
        if (isViewAttached()) {
            Intent intent = new Intent(getView().getContactActivity(), GalleryActivity.class);
            intent.putExtra(GalleryActivity.ORIGIN_ALBUM, album);
            intent.putExtra(GalleryActivity.ACTION, GalleryActivity.ACTION_NEW_ALBUM);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            getView().getContactActivity().startActivity(intent);
        }
    }

    public void switchToPublic(Album album) {
        String url = Uri.MODIFY_ALBUM + "?aid=" + album.getAid() + "&title=" + album.getTitle() +
                "&adesc=" + album.getAdesc() + "&who=" + album.getWho() + "&readPassword=" + EncryptUtils.digest("")
                + "&modPassword=" + EncryptUtils.digest("");
        mModel.switchToPublic(url, new OnRequestFinishListener() {
            @Override
            public void onSuccess(Object data) {
                mHandler.sendEmptyMessage(MSG_SUCCESS);
            }

            @Override
            public void onFail(String reason, Result result) {
                Message.obtain(mHandler, MSG_FAIL, reason).sendToTarget();
            }
        });
    }
}
