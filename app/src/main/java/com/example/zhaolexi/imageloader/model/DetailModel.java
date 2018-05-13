package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.bean.Detail;
import com.example.zhaolexi.imageloader.callback.OnLoadFinishListener;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface DetailModel<V extends Detail> {

    void loadMoreData(int page, OnLoadFinishListener<V> listener);

    void setUrl(String url);
}
