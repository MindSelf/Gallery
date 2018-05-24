package com.example.zhaolexi.imageloader.detail;

import com.example.zhaolexi.imageloader.common.base.BaseViewInterface;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public interface DetailViewInterface<V extends Detail> extends BaseViewInterface{

    void showNewData(List<V> newData, boolean isRetry);

    void showNoMoreData(String message);

    void showError(String message);
}
