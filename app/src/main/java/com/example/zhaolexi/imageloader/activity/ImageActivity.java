package com.example.zhaolexi.imageloader.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.loader.ImageLoader;

import java.io.IOException;

public class ImageActivity extends Activity {

    private final int UPDATE_MESSAGE=1;
    private String url;
    private ImageLoader imageLoader;
    private ImageView imageView;
    private Bitmap bitmap;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_MESSAGE) {
                Bitmap bitmap = (Bitmap) msg.obj;
                imageView.setImageBitmap(bitmap);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Intent intent=getIntent();
        url = intent.getStringExtra("url");
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageResource(R.drawable.image_default);
        imageLoader=ImageLoader.build(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bitmap = imageLoader.loadBitmapFromDiskCache(url);
                    if (bitmap == null) {
                        bitmap = imageLoader.downloadBitmapFromUrl(url);
                    }
                    Message msg=Message.obtain(handler,UPDATE_MESSAGE,bitmap);
                    msg.sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bitmap.recycle();
    }
}
