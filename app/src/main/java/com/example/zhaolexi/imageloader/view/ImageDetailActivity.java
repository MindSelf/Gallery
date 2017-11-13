package com.example.zhaolexi.imageloader.view;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.base.BaseActivity;
import com.example.zhaolexi.imageloader.presenter.ImageDetailPresenter;

public class ImageDetailActivity extends BaseActivity<ImageDetailViewInterface,ImageDetailPresenter> implements ImageDetailViewInterface {

    private ImageView mImageView;

    @Override
    protected void onStart() {
        super.onStart();
        String url=getIntent().getStringExtra("url");
        mPresenter.loadBitmap(url);
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
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public void showError(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
    }
}
