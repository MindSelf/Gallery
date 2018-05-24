package com.example.zhaolexi.imageloader.detail;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class DetailActivity<T extends DetailPresenter, V extends Detail> extends BaseActivity<T> implements DetailViewInterface<V>, View.OnTouchListener {

    public static final String DETAILS_KEY = "detail";
    public static final String CURRENT_INDEX = "mCurIndex";
    public static final String CURRENT_PAGE = "page";
    public static final String ALBUM_URL = "url";

    protected ArrayList<V> mlist;
    protected int mCurIndex;

    protected ViewPager mViewPager;
    protected DetailPagerAdapter<V> mPagerAdapter;

    private boolean mHasLastEventActionUp = true;
    private int mLastX;
    private int mDirection;


    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(CURRENT_INDEX, mViewPager.getCurrentItem());
        intent.putExtra(DETAILS_KEY, mlist);
        mPresenter.finish(intent);
        setResultIntent(intent);
        setResult(RESULT_OK, intent);
        super.finish();
        overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
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
                //由于onPageScrolled在action_move过程中会不断回调，所以只处理action_up的事件
                if (position == 0 && mDirection > 0 && mHasLastEventActionUp && state == ViewPager.SCROLL_STATE_DRAGGING) {
                    mHasLastEventActionUp = false;
                    showNoMoreData("已经是第一张图片了");
                } else if (position == getIndex(mPagerAdapter.getCount()) && mDirection < 0 && mHasLastEventActionUp && state == ViewPager.SCROLL_STATE_DRAGGING) {
                    mHasLastEventActionUp = false;
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
        mDirection = (int) event.getX() - mLastX;
        mLastX = (int) event.getX();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mHasLastEventActionUp = true;
        }
        return false;
    }
}
