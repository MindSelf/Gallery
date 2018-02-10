package com.example.zhaolexi.imageloader.view;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.adapter.BucketAdapter;
import com.example.zhaolexi.imageloader.adapter.PhotoAdapter;
import com.example.zhaolexi.imageloader.base.BaseActivity;
import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.bean.PhotoBucket;
import com.example.zhaolexi.imageloader.callback.OnItemClickListener;
import com.example.zhaolexi.imageloader.presenter.UploadPhotoPresenter;
import com.example.zhaolexi.imageloader.ui.SpacesItemDecoration;
import com.example.zhaolexi.imageloader.utils.MyUtils;

import java.util.List;
import java.util.Set;


public class UploadPhotoActivity extends BaseActivity<UploadPhotoPresenter> implements UploadPhotoViewInterface, View.OnClickListener, View.OnTouchListener, AdapterView.OnItemClickListener, OnItemClickListener, PhotoAdapter.OnSelectCountChangeListener, PhotoAdapter.OnDateChangedListener {

    private static final int READ_EXTERNAL_STORAGE = 1;

    private TextView mSubmit, mSelectBucket, mDate;
    private RecyclerView mPhotoList;
    private ImageView mBlock, mNavigate;
    private ListView mBucketList;
    private ViewStub mViewStub;

    private BucketAdapter mBucketAdapter;
    private PhotoAdapter mPhotoAdapter;
    private AlphaAnimation mAppearAnimation;
    private AlphaAnimation mDisappearAnimation;
    private ValueAnimator mOpenListAnimator;
    private ValueAnimator mCloseListAnimator;

    private String mUploadAid;
    private int mBucketListHeight;
    private boolean mIsListAnimating, mIsDateVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
        } else {
            mPresenter.displayAllPhotos();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPresenter.displayAllPhotos();
                } else {
                    Toast.makeText(this, "读取相册失败", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void initData() {
        mUploadAid = getIntent().getStringExtra(AlbumFragment.KEY_AID);
        mBucketListHeight = MyUtils.dp2px(this, 450);
        mPhotoAdapter = new PhotoAdapter(this);
        mPhotoAdapter.setOnItemClickListener(this);
        mPhotoAdapter.setSelectCountChangeListener(this);
        mPhotoAdapter.setOnDateChangedListener(this);
        initAnimator();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_upload_photo);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSubmit = (TextView) findViewById(R.id.submit);
        mPhotoList = (RecyclerView) findViewById(R.id.rv_list);
        mBlock = (ImageView) findViewById(R.id.list_block);
        mDate = (TextView) findViewById(R.id.tv_date);
        mSelectBucket = (TextView) findViewById(R.id.tv_select_bucket);
        mNavigate = (ImageView) findViewById(R.id.iv_navigate);
        mViewStub = (ViewStub) findViewById(R.id.stub);

        mToolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.ic_arrow_back));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.cancelTask();
                finish();
            }
        });

        mPhotoList.setAdapter(mPhotoAdapter);
        mPhotoList.setLayoutManager(new GridLayoutManager(this, 4));
        mPhotoList.addItemDecoration(new SpacesItemDecoration(MyUtils.dp2px(this, 1)));
        mPhotoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mPhotoAdapter.setIsIdle(true);
                    //滑动时onBindViewHolder先于回调方法，所以静止时要提醒Adapter更新数据
                    mPhotoAdapter.notifyDataSetChanged();
                    if (mIsDateVisible) mDate.startAnimation(mDisappearAnimation);
                } else {
                    mPhotoAdapter.setIsIdle(false);
                    if (!mIsDateVisible) mDate.startAnimation(mAppearAnimation);
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
                mBucketList.getLayoutParams().height = (int) animation.getAnimatedValue();
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

        mCloseListAnimator = ValueAnimator.ofInt(mBucketListHeight, 0).setDuration(500);
        mCloseListAnimator.setTarget(mBucketList);
        mCloseListAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBucketList.getLayoutParams().height = (int) animation.getAnimatedValue();
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
            public void onAnimationRepeat(Animator animation) {}
        });

        mAppearAnimation = new AlphaAnimation(0, 1f);
        mAppearAnimation.setDuration(500);
        mAppearAnimation.setFillAfter(true);
        mAppearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsDateVisible = true;
                mDate.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        mDisappearAnimation = new AlphaAnimation(1f, 0);
        mDisappearAnimation.setDuration(100);
        mDisappearAnimation.setFillAfter(true);
        mDisappearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsDateVisible = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDate.clearAnimation();
                mDate.setVisibility(View.GONE);}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    @Override
    protected UploadPhotoPresenter createPresenter() {
        return new UploadPhotoPresenter();
    }

    @Override
    public void showPhotos(Set<Photo> set) {
        mSubmit.setText("完成");
        mSubmit.setSelected(false);
        mSubmit.setEnabled(false);
        mPhotoList.scrollToPosition(0);     //定位到列表顶部，这个方法由LayoutManager负责实现
        mPhotoAdapter.clearSelectedSet();
        mPhotoAdapter.setData(set);
        mPhotoAdapter.notifyDataSetChanged();
    }

    @Override
    public void openBucketList(List<PhotoBucket> list) {
        if (mBucketList == null) {
            mBucketList = (ListView) mViewStub.inflate();
            mBucketAdapter = new BucketAdapter(list);
            mBucketList.setAdapter(mBucketAdapter);
            mBucketList.setOnItemClickListener(this);
            mBucketList.setItemChecked(0, true);    //初次打开时选择"所有图片"
        }
        mBlock.setVisibility(View.VISIBLE);
        mOpenListAnimator.start();
        //bucketList设置choiceMode="singleChoice"，点击时会记录当前选中数据项
        //定位到当前选中数据项的位置
        mBucketList.setSelection(mBucketList.getCheckedItemPosition());
    }

    @Override
    public void closeBucketList(boolean immediately) {
        if (!immediately) {
            mCloseListAnimator.start();
        } else {
            mBucketList.getLayoutParams().height = 0;
            mBucketList.requestLayout();
        }
        mBlock.setVisibility(View.GONE);
    }

    @Override
    public void onSelectedBucket(int position) {
        mSelectBucket.setText(mBucketAdapter.getItem(position).getName());
//        mBucketAdapter.notifyDataSetChanged();
        /*
        这里是想要通过更新布局来调用getView从而使RadioButton状态改变，但是由于之后进行关闭动画，requestLayout会触发onLayout
        来更新布局，同样会调用getView，所以这里就不重复调用了
         */
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onOverSelect() {
        Toast.makeText(this, String.format("最多只能选择%d张照片", PhotoAdapter.MAX_SIZE), Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onUploadFinish(boolean success, String msg) {
        if (success) {
            setResult(RESULT_OK);
            finish();
        } else {
            mSubmit.setText(String.format("完成(%d/%d)", mPhotoAdapter.getSelectedCount(), PhotoAdapter.MAX_SIZE));
            mSubmit.setSelected(true);
        }
        mSubmit.setEnabled(true);
        mPhotoAdapter.setIsUploading(false);
        mPhotoAdapter.notifyDataSetChanged();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getUploadAid() {
        return mUploadAid;
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void onSelectedCountChange(int size) {
        if (size > 0) {
            mSubmit.setEnabled(true);
            mSubmit.setText(String.format("完成(%d/%d)", size, PhotoAdapter.MAX_SIZE));
            mSubmit.setSelected(true);
        } else {
            mSubmit.setText("完成");
            mSubmit.setSelected(false);
            mSubmit.setEnabled(false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPresenter.selectBucket(mBucketAdapter.getItem(position), position);
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
                mPhotoAdapter.setIsUploading(true);
                mPhotoAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.tv_select_bucket: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        mSelectBucket.setTextColor(getResources().getColor(R.color.block));
                        mNavigate.setImageDrawable(getResources().getDrawable(R.mipmap.ic_arrow_drop_up_block));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mSelectBucket.setTextColor(Color.WHITE);
                        mNavigate.setImageDrawable(getResources().getDrawable(R.mipmap.ic_arrow_drop_up_white));
                        break;
                    default:
                        break;
                }
            }
        }
        return false;
    }


    @SuppressLint("DefaultLocale")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mPresenter.onBackPressed())
                    return true;
                if (mPresenter.cancelTask()) {
                    mSubmit.setText(String.format("完成(%d/%d)", mPhotoAdapter.getSelectedCount(), PhotoAdapter.MAX_SIZE));
                    mSubmit.setEnabled(true);
                    mSubmit.setSelected(true);
                    mPhotoAdapter.setIsUploading(false);
                    mPhotoAdapter.notifyDataSetChanged();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                mPresenter.chooseBucketList();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDateChanged(String curDate) {
        mDate.setText(curDate);
    }
}
