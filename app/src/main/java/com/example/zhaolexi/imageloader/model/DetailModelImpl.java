package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.bean.Detail;
import com.example.zhaolexi.imageloader.callback.OnLoadFinishListener;

import java.util.ArrayList;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public class DetailModelImpl<V extends Detail> implements DetailModel<V> {

    protected String mUrl;

    @Override
    public void setUrl(String url) {
        this.mUrl = url;
    }

    @Override
    public void loadMoreData(int page, final OnLoadFinishListener<V> listener) {

        listener.onSuccess(new ArrayList<V>());
    }
}
