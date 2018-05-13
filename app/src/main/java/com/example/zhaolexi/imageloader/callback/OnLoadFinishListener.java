package com.example.zhaolexi.imageloader.callback;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/2/7.
 */

public interface OnLoadFinishListener<T> {

    void onSuccess(List<T> data);

    void onFail(String reason);
}
