package com.example.zhaolexi.imageloader.home.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.graphics.drawable.ArgbEvaluator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.home.InteractInterface;
import com.example.zhaolexi.imageloader.home.gallery.AlbumPagerAdapter;
import com.example.zhaolexi.imageloader.home.gallery.GalleryActivity;
import com.example.zhaolexi.imageloader.redirect.access.AlbumPasswordDialog;
import com.example.zhaolexi.imageloader.redirect.router.Result;

import java.util.ArrayList;
import java.util.List;

public class AlbumManager implements AlbumManageViewInterface, ManagedAlbumAdapter.OnItemClickListener, ManagedAlbumAdapter.OnItemAddListener, View.OnClickListener {

    private GalleryActivity mActivity;
    private InteractInterface mInteract;
    private AlbumManagePresenter mPresenter;

    private ViewStub mStubGuide, mStubFinish, mStubManagedAlbum;
    private LinearLayout mContainer;
    private ImageView mManage;
    private TextView mGuide, mFinish;
    private RecyclerView mManagedAlbumList;

    private AlphaAnimation mAlphaAppear, mAlphaDisappear;
    private ValueAnimator mColorAppear, mColorDisappear, mRotateOpen, mRotateClose;
    public final long DURATION = 150L;

    private ManagedAlbumAdapter mManagedAlbumAdapter;
    private AlbumPagerAdapter mPageAdapter;
    private List<Album> mAlbumList;
    private List<Album> mRandomList;

    private boolean mIsManagePageAnimating;

    public AlbumManager(GalleryActivity activity, AlbumPagerAdapter pagerAdapter, List<Album> albumList) {
        mActivity = activity;
        mInteract = activity;
        mPageAdapter = pagerAdapter;
        mPresenter = new AlbumManagePresenter();
        initData(albumList);
        initView(activity);
        initAnimation();
    }

    private void initData(List<Album> albumList) {
        mAlbumList = albumList;
        mRandomList = new ArrayList<>();
        preLoad();
    }

    private void initView(Activity activity) {
        mContainer = (LinearLayout) activity.findViewById(R.id.ll_container);
        mManage = (ImageView) activity.findViewById(R.id.iv_manager);
        mManage.setOnClickListener(this);
        mStubGuide = (ViewStub) activity.findViewById(R.id.stub_guide);
        mStubFinish = (ViewStub) activity.findViewById(R.id.stub_finish);
        mStubManagedAlbum = (ViewStub) activity.findViewById(R.id.stub_managed_album);
    }

    private void initAnimation() {
        Interpolator linear = new LinearInterpolator();
        ValueAnimator.AnimatorUpdateListener colorListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mContainer.setBackgroundColor((int) animation.getAnimatedValue());
            }
        };
        ValueAnimator.AnimatorUpdateListener rotateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mManage.setRotation((float) animation.getAnimatedValue());
            }
        };
        Animation.AnimationListener appearAnimationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsManagePageAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsManagePageAnimating = false;
                mInteract.setIsInManagePage(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        Animation.AnimationListener disappearAnimationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsManagePageAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsManagePageAnimating = false;
                mInteract.setIsInManagePage(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        mAlphaAppear = new AlphaAnimation(0, 1);
        mAlphaAppear.setDuration(DURATION);
        mAlphaAppear.setFillAfter(true);
        mAlphaAppear.setInterpolator(linear);
        mAlphaAppear.setAnimationListener(appearAnimationListener);

        mAlphaDisappear = new AlphaAnimation(1, 0);
        mAlphaDisappear.setDuration(DURATION);
        mAlphaDisappear.setFillAfter(true);
        mAlphaDisappear.setInterpolator(linear);
        mAlphaDisappear.setAnimationListener(disappearAnimationListener);

        mRotateOpen = ValueAnimator.ofFloat(0, 45f);
        mRotateOpen.setDuration(DURATION);
        mRotateOpen.setInterpolator(linear);
        mRotateOpen.addUpdateListener(rotateListener);

        mRotateClose = ValueAnimator.ofFloat(45f, 0);
        mRotateClose.setDuration(DURATION);
        mRotateClose.setInterpolator(linear);
        mRotateClose.addUpdateListener(rotateListener);

        mColorAppear = ValueAnimator.ofInt(mActivity.getResources().getColor(R.color.colorPrimary), mActivity.getResources().getColor(R.color.windowBackground));
        mColorAppear.setEvaluator(ArgbEvaluator.getInstance());
        mColorAppear.setInterpolator(linear);
        mColorAppear.setDuration(DURATION);
        mColorAppear.addUpdateListener(colorListener);
        mColorAppear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mIsManagePageAnimating = false;
                mInteract.setIsInManagePage(true);
                mContainer.setBackgroundColor(mActivity.getResources().getColor(R.color.windowBackground));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsManagePageAnimating = false;
                mInteract.setIsInManagePage(true);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mIsManagePageAnimating = true;
            }
        });

        mColorDisappear = ValueAnimator.ofInt(mActivity.getResources().getColor(R.color.windowBackground), mActivity.getResources().getColor(R.color.colorPrimary));
        mColorDisappear.setEvaluator(ArgbEvaluator.getInstance());
        mColorDisappear.setInterpolator(linear);
        mColorDisappear.setDuration(DURATION);
        mColorDisappear.addUpdateListener(colorListener);
        mColorDisappear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mIsManagePageAnimating = false;
                mInteract.setIsInManagePage(false);
                mContainer.setBackgroundColor(mActivity.getResources().getColor(R.color.colorPrimary));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsManagePageAnimating = false;
                mInteract.setIsInManagePage(false);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mIsManagePageAnimating = true;
            }
        });
    }

    @Override
    public void attachPresenter() {
        mPresenter.attachView(this);
    }

    @Override
    public void detachPresenter() {
        mPresenter.detachView();
    }

    @Override
    public void showManagePage(boolean animated) {

        if (mInteract.onShowManagerPage(mPresenter.getCurrentPage()) && mManagedAlbumAdapter != null) {
            mManagedAlbumAdapter.notifyDataSetChanged();
        }

        initManagePage();
        if (!animated) {
            mManage.setRotation(45f);
            mContainer.setBackgroundColor(mActivity.getResources().getColor(R.color.windowBackground));
            mInteract.setIsInManagePage(true);
        } else {
            mColorAppear.start();
            mRotateOpen.start();
            mGuide.startAnimation(mAlphaAppear);
            mManagedAlbumList.startAnimation(mAlphaAppear);
        }
    }

    private void initManagePage() {
        if (mGuide == null) {
            mGuide = (TextView) mStubGuide.inflate();
        }
        if (mManagedAlbumList == null) {
            mManagedAlbumList = (RecyclerView) mStubManagedAlbum.inflate();
            mManagedAlbumAdapter = new ManagedAlbumAdapter(this, mAlbumList, mRandomList);
            mManagedAlbumAdapter.setOnItemClickListener(this);
            mManagedAlbumAdapter.setOnItemAddListener(this);
            mManagedAlbumList.setAdapter(mManagedAlbumAdapter);
            GridLayoutManager manager = new GridLayoutManager(mActivity, 3);
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (mManagedAlbumAdapter.getItemViewType(position) == ManagedAlbumAdapter.TYPE_HINT) {
                        return 3;
                    } else {
                        return 1;
                    }
                }
            });
            mManagedAlbumList.setLayoutManager(manager);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new AlbumItemTouchHelperCallback(mManagedAlbumAdapter));
            itemTouchHelper.attachToRecyclerView(mManagedAlbumList);
            mManagedAlbumAdapter.setItemTouchHelper(itemTouchHelper);
        }
        mGuide.setVisibility(View.VISIBLE);
        mManagedAlbumList.setVisibility(View.VISIBLE);
        if (mAlbumList.isEmpty()) {
            mGuide.setText(R.string.manage_guide_empty);
            mManage.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismissManagePage() {
        mColorDisappear.start();
        mRotateClose.start();
        mGuide.startAnimation(mAlphaDisappear);
        mManagedAlbumList.startAnimation(mAlphaDisappear);
        mGuide.clearAnimation();
        mGuide.setVisibility(View.GONE);
        mManagedAlbumList.clearAnimation();
        mManagedAlbumList.setVisibility(View.GONE);

        mInteract.onDismissManagerPage(mPresenter.getCurrentPage());

        preLoad();
    }

    @Override
    public void showRandom(List<Album> albums) {
        mRandomList.clear();
        mRandomList.addAll(albums);
        if (mManagedAlbumAdapter != null) {
            int positionStart = mManagedAlbumAdapter.getLocalAlbumCount() + 2;
            mManagedAlbumAdapter.notifyItemRangeChanged(positionStart, albums.size());
        }
    }

    @Override
    public void showError(String reason) {
        Toast.makeText(mActivity, reason, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAlbumListStateChanged(boolean isEmpty, boolean editable) {
        if (mFinish == null) {
            mFinish = (TextView) mStubFinish.inflate();
            mFinish.setOnClickListener(this);
        }

        if (isEmpty) {
            mGuide.setText(R.string.manage_guide_empty);
            mFinish.setVisibility(View.GONE);
            mManage.setVisibility(View.GONE);
            mManagedAlbumAdapter.setEditable(false, false);
        } else if (editable) {
            mGuide.setText(R.string.manage_guide_editable);
            mFinish.setVisibility(View.VISIBLE);
            mManage.setVisibility(View.GONE);
        } else {
            mGuide.setText(R.string.manage_guide_uneditable);
            mFinish.setVisibility(View.GONE);
            mManage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mManagedAlbumAdapter != null && mManagedAlbumAdapter.isEditable() && !mAlbumList.isEmpty()) {
            mManagedAlbumAdapter.setEditable(false, true);
            return true;
        } else if (mInteract.isInManagePage() && !mAlbumList.isEmpty()) {
            dismissManagePage();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ManagedAlbumAdapter getAlbumAdapter() {
        return mManagedAlbumAdapter;
    }

    @Override
    public AlbumPagerAdapter getPagerAdapter() {
        return mPageAdapter;
    }

    private void preLoad() {
        mPresenter.getRandom();
    }

    @Override
    public Activity getContactActivity() {
        return mActivity;
    }

    @Override
    public AlbumManagePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onItemClick(boolean isFromRandom, int position, boolean editable) {
        if (!editable) {
            //非编辑状态下点击相册跳转到相册页面，如果该相册在本地相册中不存在（即随机相册），则将该相册添加到本地相册中
            Album album = mManagedAlbumAdapter.getAlbum(position);
            mPresenter.addAlbum(album);
            dismissManagePage();
        } else if (!isFromRandom) {
            //编辑状态下点击本地相册则移除相册
            mPresenter.removeAlbum(position);
        } else {
            //编辑状态下点击随机相册则添加到本地相册中（可增加移动动画）
            mPresenter.addAlbum(mManagedAlbumAdapter.removeAlbum(position));
        }
    }

    @Override
    public void onClickItemAdd() {
        new AlbumPasswordDialog.Builder(mActivity)
                .setTitle(mActivity.getString(R.string.add_album))
                .setCallback(new OnRequestFinishListener<Album>() {
                    @Override
                    public void onSuccess(Album album) {
                        if (album != null) {
                            mPresenter.addAlbum(album);
                        }
                    }

                    @Override
                    public void onFail(String reason, Result result) {
                    }
                })
                .build().show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_manager:
                if (!mIsManagePageAnimating) {
                    if (!mInteract.isInManagePage()) {
                        showManagePage(true);
                    } else {
                        dismissManagePage();
                    }
                }
                break;
            case R.id.tv_finish:
                mManagedAlbumAdapter.setEditable(false, true);
                if (mPresenter.shouldUpdateState()) {
                    mPageAdapter.notifyDataSetChanged();
                    mPresenter.clearUpdateState();
                }
                break;
        }
    }
}
