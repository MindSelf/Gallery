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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.adapter.BucketAdapter;
import com.example.zhaolexi.imageloader.callback.OnItemClickListener;
import com.example.zhaolexi.imageloader.adapter.PhotoAdapter;
import com.example.zhaolexi.imageloader.base.BaseActivity;
import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.bean.PhotoBucket;
import com.example.zhaolexi.imageloader.presenter.UploadPhotoPresenter;
import com.example.zhaolexi.imageloader.ui.SpacesItemDecoration;
import com.example.zhaolexi.imageloader.utils.MyUtils;

import java.util.List;


public class UploadPhotoActivity extends BaseActivity<UploadPhotoPresenter> implements UploadPhotoViewInterface, View.OnClickListener, View.OnTouchListener, AdapterView.OnItemClickListener, OnItemClickListener, PhotoAdapter.OnSelectCountChangeListner {

    private static final int READ_EXTERNAL_STORAGE = 1;

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

    private String mUploadAid;
    private int mBucketListHeight;
    private boolean mIsListAnimating;

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
        mPhotoAdapter.setOnItemClickListner(this);
        mPhotoAdapter.setSelectCountChangeListener(this);
        initAnimator();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_upload_photo);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSubmit = (TextView) findViewById(R.id.submit);
        mPhotoList = (RecyclerView) findViewById(R.id.rv_list);
        mSelectBucket = (TextView) findViewById(R.id.tv_select_bucket);
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
        mPhotoList.setLayoutManager(new GridLayoutManager(this, 3));
        mPhotoList.addItemDecoration(new SpacesItemDecoration(MyUtils.dp2px(this, 1)));
        mPhotoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mPhotoAdapter.setIsIdle(true);
                    //滑动时onBindViewHolder先于回调方法，所以静止时要提醒Adapter更新数据
                    mPhotoAdapter.notifyDataSetChanged();
                } else {
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
    protected UploadPhotoPresenter createPresenter() {
        return new UploadPhotoPresenter();
    }

    @Override
    public void showPhotos(List<Photo> list) {
        mSubmit.setText("完成");
        mSubmit.setSelected(false);
        mSubmit.setEnabled(false);
        mPhotoList.scrollToPosition(0);     //定位到列表顶部，这个方法由LayoutManager负责实现
        mPhotoAdapter.clearSelectedSet();
        mPhotoAdapter.setDatas(list);
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

}
