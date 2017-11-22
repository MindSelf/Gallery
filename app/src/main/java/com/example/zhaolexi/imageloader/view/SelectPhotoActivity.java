package com.example.zhaolexi.imageloader.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.adapter.BucketAdapter;
import com.example.zhaolexi.imageloader.adapter.OnItemClickListener;
import com.example.zhaolexi.imageloader.adapter.PhotoAdapter;
import com.example.zhaolexi.imageloader.base.BaseActivity;
import com.example.zhaolexi.imageloader.bean.MessageEvent;
import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.bean.PhotoBucket;
import com.example.zhaolexi.imageloader.presenter.SeletePhotoPresenter;
import com.example.zhaolexi.imageloader.ui.SpacesItemDecoration;
import com.example.zhaolexi.imageloader.utils.MyUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


public class SelectPhotoActivity extends BaseActivity<SelectPhotoViewInterface,SeletePhotoPresenter> implements SelectPhotoViewInterface, View.OnClickListener, View.OnTouchListener, AdapterView.OnItemClickListener, OnItemClickListener, PhotoAdapter.OnSelectCountChangeListner {

    private Toolbar mToolbar;
    private TextView mSubmit;
    private RecyclerView mPhotoList;
    private ListView mBucketList;
    private TextView mSelectBucket;
    private ViewStub mViewStub;

    private BucketAdapter mBucketAdapter;
    private PhotoAdapter mPhotoAdapter;
    private ValueAnimator mOpenListAnimator;
    private ValueAnimator mCloseListAnimator;

    private int mBucketListHeight;
    private boolean mIsListAnimating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.displayAllPhotos();
    }

    @Override
    protected void initData() {
        mBucketListHeight= MyUtils.dp2px(this,450);
        mPhotoAdapter = new PhotoAdapter(this);
        mPhotoAdapter.setOnItemClickListner(this);
        mPhotoAdapter.setSelectCountChangeListener(this);
        initAnimator();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_select_photo);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSubmit = (TextView) findViewById(R.id.submit);
        mPhotoList = (RecyclerView) findViewById(R.id.rv_list);
        mSelectBucket = (TextView) findViewById(R.id.tv_select_bucket);
        mViewStub = (ViewStub) findViewById(R.id.stub);

        mToolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.ic_arrow_back));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPhotoList.setAdapter(mPhotoAdapter);
        mPhotoList.setLayoutManager(new GridLayoutManager(this, 3));
        mPhotoList.addItemDecoration(new SpacesItemDecoration(MyUtils.dp2px(this,1)));
        mPhotoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE) {
                    mPhotoAdapter.setIsIdle(true);
                    //滑动时onBindViewHolder先于回调方法，所以静止时要提醒Adapter更新数据
                    mPhotoAdapter.notifyDataSetChanged();
                }else{
                    mPhotoAdapter.setIsIdle(false);
                }
            }
        });
        mSubmit.setOnClickListener(this);
        mSelectBucket.setOnClickListener(this);
        mSelectBucket.setOnTouchListener(this);

    }

    private void initAnimator() {
        mOpenListAnimator = ValueAnimator.ofInt(0, mBucketListHeight).setDuration(200);
        mOpenListAnimator.setTarget(mBucketList);
        mOpenListAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                mBucketList.getLayoutParams().height = height;
                mBucketList.requestLayout();
            }
        });
        mOpenListAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsListAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsListAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mIsListAnimating = false;
                mBucketList.getLayoutParams().height = mBucketListHeight;
                mBucketList.requestLayout();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mCloseListAnimator = ValueAnimator.ofInt(mBucketListHeight, 0).setDuration(200);
        mCloseListAnimator.setTarget(mBucketList);
        mCloseListAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                mBucketList.getLayoutParams().height = height;
                mBucketList.requestLayout();
            }
        });
        mCloseListAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsListAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsListAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mIsListAnimating = false;
                mBucketList.getLayoutParams().height = 0;
                mBucketList.requestLayout();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    protected SeletePhotoPresenter createPresenter() {
        return new SeletePhotoPresenter();
    }

    @Override
    public void showPhotos(List<Photo> list) {
        mSubmit.setText("完成");
        mSubmit.setSelected(false);
        mSubmit.setEnabled(false);
        mPhotoAdapter.clearSelectedSet();
        mPhotoAdapter.setDatas(list);
        mPhotoAdapter.notifyDataSetChanged();
    }

    @Override
    public void openBucketList(List<PhotoBucket> list) {
        if (mBucketList == null) {
            mBucketList=(ListView) mViewStub.inflate();
            mBucketAdapter =new BucketAdapter(list);
            mBucketList.setAdapter(mBucketAdapter);
            mBucketList.setOnItemClickListener(this);
        }
        mOpenListAnimator.start();
    }

    @Override
    public void closeBucketList(boolean immediately) {
        if(!immediately) {
            mCloseListAnimator.start();
        }else{
            mBucketList.getLayoutParams().height=0;
            mBucketList.requestLayout();
        }
    }

    @Override
    public void changeSelectedBucket(int position) {
        mSelectBucket.setText(mBucketAdapter.getItem(position).getName());
        mBucketAdapter.setSelectPostiion(position);
        mBucketAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOverSelect() {
        Toast.makeText(this, String.format("最多只能选择%d张照片",PhotoAdapter.MAX_SIZE), Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onUploadFinish(boolean success,String msg) {
        if(success) {
            finish();
            EventBus.getDefault().post(new MessageEvent());
        }else {
            mSubmit.setText(String.format("完成(%d/%d)", mPhotoAdapter.getSelectedCount(),PhotoAdapter.MAX_SIZE));
            mSubmit.setSelected(true);
        }
        mSubmit.setEnabled(true);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void onSelectedCountChange(int size) {
        if (size > 0) {
            mSubmit.setEnabled(true);
            mSubmit.setText(String.format("完成(%d/%d)",size,PhotoAdapter.MAX_SIZE));
            mSubmit.setSelected(true);
        }else{
            mSubmit.setText("完成");
            mSubmit.setSelected(false);
            mSubmit.setEnabled(false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPresenter.selectBucket(mBucketAdapter.getItem(position),position);
    }


    @Override
    public void onItemClick(View view, int position) {
        mPresenter.openDetail(mPhotoAdapter.getItem(position).getPath());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_bucket:
                if (!mIsListAnimating) {
                    mPresenter.chooseBucketList();
                }
                break;
            case R.id.submit:
                mSubmit.setSelected(false);
                mSubmit.setEnabled(false);
                mSubmit.setText("上传中...");
                mPresenter.upLoadImage(mPhotoAdapter.getSelectedPhotos());
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.tv_select_bucket:{
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        mSelectBucket.setTextColor(Color.parseColor("#60000000"));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mSelectBucket.setTextColor(Color.WHITE);
                        break;
                    default:
                        break;
                }
            }
        }
        return false;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mPresenter.onBackPressed())
                    return true;
                if (mPresenter.cancleTask()) {
                    mSubmit.setText(String.format("完成(%d/%d)", mPhotoAdapter.getSelectedCount(),PhotoAdapter.MAX_SIZE));
                    mSubmit.setEnabled(true);
                    mSubmit.setSelected(true);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                mPresenter.chooseBucketList();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
