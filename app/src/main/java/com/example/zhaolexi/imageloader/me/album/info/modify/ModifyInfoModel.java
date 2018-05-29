package com.example.zhaolexi.imageloader.me.album.info.modify;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;

public interface ModifyInfoModel {

    void setUri(String uri);

    void modify(String text, OnRequestFinishListener listener);
}
