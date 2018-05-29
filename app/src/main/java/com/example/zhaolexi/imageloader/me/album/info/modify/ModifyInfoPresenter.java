package com.example.zhaolexi.imageloader.me.album.info.modify;

import android.os.Message;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.common.utils.EncryptUtils;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.redirect.router.Result;

public class ModifyInfoPresenter extends BasePresenter<ModifyInfoViewInterface, ModifyInfoModel> {

    @Override
    protected void onMessageSuccess(Message msg) {
        if (isViewAttached()) {
            getView().onModifySuccess();
        }
    }

    @Override
    protected void onMessageFail(Message msg) {
        if (isViewAttached()) {
            getView().onModifyFail((String) msg.obj);
        }
    }

    @Override
    protected ModifyInfoModel newModel() {
        return new ModifyInfoModelImpl();
    }

    public void initUri(Album albumInfo, int type) {
        String uri = Uri.MODIFY_ALBUM + "?aid=" + albumInfo.getAid() + "&who=" + albumInfo.getWho() + "&readPassword=" + EncryptUtils.digest("")
                + "&modPassword=" + EncryptUtils.digest("");
        switch (type) {
            case ModifyInfoActivity.TYPE_DESCRIPTION:
                uri = uri + "&title=" + albumInfo.getTitle() + "&adesc=%s";
                break;
            case ModifyInfoActivity.TYPE_TITLE:
                uri = uri + "&adesc=" + albumInfo.getAdesc() + "&title=%s";
                break;
        }
        mModel.setUri(uri);
    }

    public boolean modify(int type, String text) {
        if (checkLength(type, text)) {
            mModel.modify(text, new OnRequestFinishListener() {
                @Override
                public void onSuccess(Object data) {
                    mHandler.sendEmptyMessage(MSG_SUCCESS);
                }

                @Override
                public void onFail(String reason, Result result) {
                    Message.obtain(mHandler, MSG_FAIL, reason).sendToTarget();
                }
            });
            return true;
        } else if (type == ModifyInfoActivity.TYPE_TITLE) {
            Toast.makeText(BaseApplication.getContext(), "相册名不能超过15个字符", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(BaseApplication.getContext(), "相册描述不能超过100个字符", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean checkLength(int type, String text) {
        int length = text.length();
        switch (type) {
            case ModifyInfoActivity.TYPE_DESCRIPTION:
                return length > 0 && length <= 100;
            case ModifyInfoActivity.TYPE_TITLE:
                return length > 0 && length <= 15;
        }
        return true;
    }
}
