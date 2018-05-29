package com.example.zhaolexi.imageloader.common.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.example.zhaolexi.imageloader.common.base.BaseApplication;

public class ClipboardUtils {

    public static void clip(String text) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) BaseApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", text);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
        }
    }
}
