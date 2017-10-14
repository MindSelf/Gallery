package com.example.zhaolexi.imageloader.ImageActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.loader.ImageLoader;

public class ImageDetailActivity extends Activity  {

    private String url;
    private ImageLoader imageLoader;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        Intent intent=getIntent();
        url = intent.getStringExtra("url");
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageResource(R.drawable.image_default);
        imageLoader=ImageLoader.build(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //加载原图
                imageLoader.bindBitmap(url,imageView);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
