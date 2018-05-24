package com.example.zhaolexi.imageloader.detail;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface DetailModel<V extends Detail> {

    void loadMoreData(int page, OnRequestFinishListener<List<V>> listener);

    void setUrl(String url);
}
