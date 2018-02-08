package com.example.zhaolexi.imageloader.callback;

/**
 * Created by ZHAOLEXI on 2018/2/7.
 */

public interface OnLoadFinishListener<T> {

    void onSuccess(T data);

    void onFail(String reason);
}
