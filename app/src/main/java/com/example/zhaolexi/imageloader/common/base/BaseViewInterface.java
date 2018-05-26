package com.example.zhaolexi.imageloader.common.base;

import android.app.Activity;

public interface BaseViewInterface<T extends BasePresenter> {
    Activity getContactActivity();

    T getPresenter();
}
