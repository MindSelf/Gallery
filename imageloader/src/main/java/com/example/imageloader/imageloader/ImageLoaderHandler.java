package com.example.imageloader.imageloader;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.example.imageloader.R;

public class ImageLoaderHandler extends Handler {

    private static final String TAG = "ImageLoaderHandler";
    static final int TAG_KEY_URI = R.id.tag_key;
    static final int MESSAGE_POST_RESULT = 1;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_POST_RESULT:
                Result result = (Result) msg.obj;
                ImageView imageView = result.imageView;

                //判断url是否发生变化，避免将图片绑定到复用的item上
                String uri = (String) imageView.getTag(TAG_KEY_URI);
                if (uri.equals(result.uri)) {
                    imageView.setImageBitmap(result.bitmap);
                } else {
                    Log.d(TAG, "set image bitmap,but url has changed, ignored!");
                }
                break;
            default:
                super.handleMessage(msg);
        }

    }
}
