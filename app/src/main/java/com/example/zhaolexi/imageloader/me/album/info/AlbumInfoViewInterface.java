package com.example.zhaolexi.imageloader.me.album.info;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;

public interface AlbumInfoViewInterface extends BaseViewInterface<AlbumInfoPresenter> {

    void onSwitchToPublicSuccess();

    void onSwitchToPublicFail(String reason);
}
