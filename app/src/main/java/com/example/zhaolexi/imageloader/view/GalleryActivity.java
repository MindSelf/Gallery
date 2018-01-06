package com.example.zhaolexi.imageloader.view;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.adapter.ImageAdapter;
import com.example.zhaolexi.imageloader.adapter.OnItemClickListener;
import com.example.zhaolexi.imageloader.base.BaseActivity;
import com.example.zhaolexi.imageloader.bean.Image;
import com.example.zhaolexi.imageloader.bean.MessageEvent;
import com.example.zhaolexi.imageloader.presenter.ImagePresenter;
import com.example.zhaolexi.imageloader.ui.GridItemTouchHelperCallback;
import com.example.zhaolexi.imageloader.ui.PasswordDialog;
import com.example.zhaolexi.imageloader.ui.SpacesItemDecoration;
import com.example.zhaolexi.imageloader.utils.MyUtils;
import com.example.zhaolexi.imageloader.utils.SharePreferencesUtils;
import com.example.zhaolexi.imageloader.utils.Uri;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class GalleryActivity extends BaseActivity<GalleryViewInterface, ImagePresenter> implements GalleryViewInterface, OnItemClickListener, PasswordDialog.Builder.OnResponseListener {

    private RecyclerView mImageList;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private SwipeRefreshLayout mSwipeRefresh;
    private AlertDialog.Builder mAlertBuilder;
    private PasswordDialog.Builder mPasswordBuilder;
    private ImageAdapter mAdapter;
    private boolean mIsLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!MyUtils.isWifi(this))
            mAlertBuilder.show();
        else
            mPresenter.refresh();
    }

    @Override
    protected void initData() {
        EventBus.getDefault().register(this);
    }

    //当然也可以用onRestart
    @Subscribe
    public void onMessageEventMain(MessageEvent event) {
        mPresenter.refresh();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected ImagePresenter createPresenter() {
        return new ImagePresenter();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_image);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mFab = (FloatingActionButton) findViewById(R.id.add_photos);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(SharePreferencesUtils.getString("aid", "")))
                    mPresenter.addPhoto();
            }
        });

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.refresh();
            }
        });

        mImageList = (RecyclerView) findViewById(R.id.recyclerview);
        mImageList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mImageList.addItemDecoration(new SpacesItemDecoration(MyUtils.dp2px(this, 4)));
        mImageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mAdapter.setIsIdle(true);
                    //滑动时onBindViewHolder先于回调方法，所以静止时要提醒Adapter更新数据
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.setIsIdle(false);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int lastVisiblePosition = getLastVisiblePosition();
                if (!mIsLoading && lastVisiblePosition + 1 == mAdapter.getItemCount() && dy > 0) {
                    mPresenter.loadMore();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mAdapter = new ImageAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mImageList.setAdapter(mAdapter);

        GridItemTouchHelperCallback callback = new GridItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mImageList);

        mAlertBuilder = new AlertDialog.Builder(this);
        mAlertBuilder.setCancelable(false);
        mAlertBuilder.setMessage("您当前为非Wifi环境，是否继续加载图片");
        mAlertBuilder.setTitle("注意");
        mAlertBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.refresh();
            }
        });
        mAlertBuilder.setNegativeButton("否", null);

        mPasswordBuilder = new PasswordDialog.Builder(this);
    }

    private int getLastVisiblePosition() {
        int position = 0;
        if (mImageList.getLayoutManager() instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) mImageList.getLayoutManager()).findLastVisibleItemPosition();
        } else if (mImageList.getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) mImageList.getLayoutManager()).findLastVisibleItemPosition();
        } else if (mImageList.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
            //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mImageList.getLayoutManager();
            int[] lastPositions = layoutManager.findLastVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            int max = lastPositions[0];
            for (int i : lastPositions) {
                if (i > max)
                    max = i;
            }
            position = max;
        }
        return position;

    }

    @Override
    public void showNewDatas(boolean hasMore, List<Image> newDatas) {
        mIsLoading = false;
        mAdapter.addImages(newDatas);
        if (hasMore) {
            mAdapter.setFooterState(ImageAdapter.FOOTER_NEWDATA);
        } else {
            mAdapter.setFooterState(ImageAdapter.FOOTER_NODATA);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showError() {
        mIsLoading = false;
        mAdapter.setFooterState(ImageAdapter.FOOTER_ERROR);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoading() {
        mIsLoading = true;
        mAdapter.setFooterState(ImageAdapter.FOOTER_LOADING);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        mSwipeRefresh.setRefreshing(isRefreshing);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_album:
                mPasswordBuilder.setTitle("创建相册")
                        .setUrl(Uri.Add_Album)
                        .setOnResponseListener(this)
                        .build().show();
                break;
            case R.id.open_album:
                mPasswordBuilder.setTitle("进入相册")
                        .setUrl(Uri.Open_Album)
                        .setOnResponseListener(this)
                        .build().show();
                break;
            case R.id.random:
                mPresenter.setUrl(Uri.Girls);
                mPresenter.refresh();
                SharePreferencesUtils.putString(SharePreferencesUtils.Url, Uri.Girls);
                SharePreferencesUtils.putString(SharePreferencesUtils.Album, "");
                break;
        }
        return true;
    }

    @Override
    public void onItemClick(View view, int position) {
        Image image = mAdapter.getItem(position);
        if (image.getFullUrl() != null) {
            mPresenter.openDetail(true, image.getFullUrl());
        } else {
            mPresenter.openDetail(false, image.getThumbUrl());
        }
    }

    @Override
    public void onSuccess(String aid) {
        if (!TextUtils.isEmpty(aid)) {
            String newURL = Uri.Load_Img + "&album.aid=" + aid + "&currPage=";
            mPresenter.setUrl(newURL);
            mPresenter.refresh();
            SharePreferencesUtils.putString(SharePreferencesUtils.Url, newURL);
            SharePreferencesUtils.putString(SharePreferencesUtils.Album, aid);
        }
    }

    public ImageAdapter getAdapter() {
        return mAdapter;
    }
}
