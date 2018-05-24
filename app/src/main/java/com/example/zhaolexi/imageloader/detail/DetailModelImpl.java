package com.example.zhaolexi.imageloader.detail;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;

import java.util.ArrayList;
import java.util.List;

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
    public void loadMoreData(int page, final OnRequestFinishListener<List<V>> listener) {
        listener.onSuccess(new ArrayList<V>());
    }
}
