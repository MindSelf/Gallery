package com.example.zhaolexi.imageloader.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.base.BaseActivity;
import com.example.zhaolexi.imageloader.presenter.ImageDetailPresenter;

public class ImageDetailActivity extends BaseActivity<ImageDetailViewInterface,ImageDetailPresenter> implements ImageDetailViewInterface {

    private ImageView mImageView;
    private String mUrl;
    private boolean mHasFullImg;
    private int mMaxWidth;
    private int mMaxHeight;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mHasFullImg) {
            mPresenter.loadFullImage(mUrl, mImageView, mMaxWidth, mMaxHeight);
        }else {
            mPresenter.loadBitmapFromDiskCache(mUrl, mMaxWidth, mMaxHeight);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBitmap!=null) {
            mBitmap.recycle();
        }
    }

    @Override
    protected void initData() {
        mUrl=getIntent().getStringExtra("url");
        mHasFullImg = getIntent().getBooleanExtra("hasFullImg",false);
        mMaxWidth= 700;
        mMaxHeight=0;
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_image_detail);
        mImageView = (ImageView) findViewById(R.id.image);
        mImageView.setImageResource(R.drawable.image_default);
    }

    @Override
    protected ImageDetailPresenter createPresenter() {
        return new ImageDetailPresenter();
    }

    @Override
    public void showImage(Bitmap bitmap) {
        if(bitmap!=null) {
            mBitmap=bitmap;
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void showError(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
    }
}
