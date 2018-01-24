package com.example.zhaolexi.imageloader.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.base.BaseActivity;
import com.example.zhaolexi.imageloader.presenter.ImageDetailPresenter;
import com.example.zhaolexi.imageloader.utils.MyUtils;
import com.example.zhaolexi.imageloader.utils.loader.ImageLoader;

public class ImageDetailActivity extends BaseActivity<ImageDetailViewInterface, ImageDetailPresenter> implements ImageDetailViewInterface {

    private ImageView mImageView;
    private String mUrl;
    private boolean mHasFullImg;
    private int mMaxWidth;
    private int mMaxHeight;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageLoader.TaskOptions options = new ImageLoader.TaskOptions(mMaxWidth, mMaxHeight);
        options.scaleType = ImageLoader.TaskOptions.ScaleType.CENTER_INSIDE;
        if (mHasFullImg) {
            mPresenter.loadFullImage(mUrl, mImageView, options);
        } else {
            mPresenter.loadBitmapFromDiskCache(mUrl, options);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }

    @Override
    protected void initData() {
        mUrl = getIntent().getStringExtra("url");
        mHasFullImg = getIntent().getBooleanExtra("hasFullImg", false);
        mMaxWidth = MyUtils.getScreenMetrics(this).widthPixels - MyUtils.dp2px(this, 40);
        mMaxHeight = 0;
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_detail);
        mImageView = (ImageView) findViewById(R.id.image);
        mImageView.setImageResource(R.mipmap.image_default);
    }

    @Override
    protected ImageDetailPresenter createPresenter() {
        return new ImageDetailPresenter();
    }

    @Override
    public void showImage(Bitmap bitmap) {
        if (bitmap != null) {
            mBitmap = bitmap;
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void showError(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
    }
}
