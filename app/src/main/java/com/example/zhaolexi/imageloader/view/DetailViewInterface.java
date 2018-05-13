package com.example.zhaolexi.imageloader.view;

import com.example.zhaolexi.imageloader.bean.Detail;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface DetailViewInterface<V extends Detail> {

    void showNewData(List<V> newData, boolean isRetry);

    void showNoMoreData(String message);

    void showError(String message);
}
