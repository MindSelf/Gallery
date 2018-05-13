package com.example.zhaolexi.imageloader.view;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.adapter.DetailPagerAdapter;
import com.example.zhaolexi.imageloader.base.BaseActivity;
import com.example.zhaolexi.imageloader.bean.Detail;
import com.example.zhaolexi.imageloader.presenter.DetailPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class DetailActivity<T extends DetailPresenter, V extends Detail> extends BaseActivity<T> implements DetailViewInterface<V>, View.OnTouchListener {

    public static final String DETAILS_KEY = "detail";
    public static final String CURRENT_INDEX = "mCurIndex";
    public static final String CURRENT_PAGE = "page";
    public static final String ALBUM_URL = "url";

    private ArrayList<V> mlist;
    protected int mCurIndex;
    private boolean hasLastEventActionUp = true;

    protected ViewPager mViewPager;
    protected DetailPagerAdapter<V> mPagerAdapter;


    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(CURRENT_INDEX, mViewPager.getCurrentItem());
        intent.putExtra(DETAILS_KEY, mlist);
        mPresenter.finish(intent);
        setResultIntent(intent);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    protected abstract void setResultIntent(Intent intent);

    @Override
    protected void initData() {
        Intent intent = getIntent();
        mlist = (ArrayList<V>) intent.getSerializableExtra(DETAILS_KEY);
        mCurIndex = intent.getIntExtra(CURRENT_INDEX, 0);
        mPresenter.setUrl(intent.getStringExtra(ALBUM_URL));
        mPresenter.setCurrentPage(intent.getIntExtra(CURRENT_PAGE, 0));
        mPagerAdapter = new DetailPagerAdapter<>(mlist);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_detail);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //需要在setNavigationOnClickListener之前设置
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        mToolbar.setNavigationIcon(R.mipmap.ic_close_white);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.vp_detail);
        mViewPager.setOnTouchListener(this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mCurIndex);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int state;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0 && positionOffset == 0 && state == ViewPager.SCROLL_STATE_DRAGGING && hasLastEventActionUp) {
                    hasLastEventActionUp = false;
                    showNoMoreData("已经是第一张图片了");
                } else if (position == getIndex(mPagerAdapter.getCount()) && state == ViewPager.SCROLL_STATE_DRAGGING && hasLastEventActionUp) {
                    hasLastEventActionUp = false;
                    mPresenter.onOverScroll();
                }
            }

            @Override
            public void onPageSelected(int position) {
                //进入倒数第二张图片就开始预加载
                if (position >= getIndex(mPagerAdapter.getCount()) - 1) {
                    mPresenter.loadMoreData(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                this.state = state;
            }

            private int getIndex(int count) {
                return count - 1;
            }
        });
    }

    @Override
    public void showNewData(List<V> newData, boolean isRetry) {
        mPagerAdapter.addDetailInfos(newData);
        if (isRetry) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNoMoreData(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            hasLastEventActionUp = true;
        }
        return false;
    }
}
