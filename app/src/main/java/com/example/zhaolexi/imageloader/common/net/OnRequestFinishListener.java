package com.example.zhaolexi.imageloader.common.net;

import com.example.zhaolexi.imageloader.redirect.router.Result;

/**
 * Created by ZHAOLEXI on 2018/2/7.
 */

public interface OnRequestFinishListener<T> {

    void onSuccess(T data);

    /**
     * @param reason
     * @param result request error or needn't redirect, result can be null!
     */
    void onFail(String reason, Result result);
}
