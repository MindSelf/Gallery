package com.example.zhaolexi.imageloader.me.album.info.modify;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;

public interface ModifyInfoViewInterface extends BaseViewInterface<ModifyInfoPresenter> {

    void onModifySuccess();

    void onModifyFail(String reason);
}
