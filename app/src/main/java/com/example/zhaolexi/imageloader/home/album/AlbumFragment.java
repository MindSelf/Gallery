package com.example.zhaolexi.imageloader.home.album;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseFragment;
import com.example.zhaolexi.imageloader.common.ui.OnItemClickListener;
import com.example.zhaolexi.imageloader.common.ui.SpacesItemDecoration;
import com.example.zhaolexi.imageloader.common.utils.DisplayUtils;
import com.example.zhaolexi.imageloader.detail.DetailActivity;
import com.example.zhaolexi.imageloader.home.InteractInterface;
import com.example.zhaolexi.imageloader.home.manager.Album;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/1/25.
 */

public class AlbumFragment extends BaseFragment<AlbumPresenter> implements AlbumViewInterface, OnItemClickListener {

    public static final String KEY_ALBUM = "album";

    private AlertDialog.Builder mAlertBuilder;
    private RecyclerView mImageList;
    private SwipeRefreshLayout mSwipeRefresh;
    private Album mAlbumInfo;

    private PhotoAdapter mAdapter;

    private boolean mIsFirstLoad = true;

    @Override
    protected AlbumPresenter createPresenter() {
        return new AlbumPresenter();
    }

    @Override
    protected int getResId() {
        return R.layout.fragment_album;
    }

    @Override
    protected void lazyLoad() {
        //如果已经加载过就不再加载，避免滑动时反复加载
        if (mIsFirstLoad) {
            mPresenter.refresh();
            mIsFirstLoad = false;
        }
    }

    @Override
    protected void initData() {
        mAlbumInfo = (Album) getArguments().getSerializable(KEY_ALBUM);
        mPresenter.setUrl(mAlbumInfo.getUrl());
    }

    @Override
    protected void initView(View contentView) {

        mSwipeRefresh = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.refresh();
            }
        });

        mImageList = (RecyclerView) contentView.findViewById(R.id.recyclerview);
        mImageList.addItemDecoration(new SpacesItemDecoration(DisplayUtils.dp2px(getContext(), 4)));
        mImageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
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
                //当item都显示在屏幕中时，onScrolled不会触发，所以在footer中添加点击重试的链接
                //当然也可以给RecyclerView设置OnTouchListener
                //getItemCount-1表示footer的位置
                if (mAdapter.getFooterState() == PhotoAdapter.FOOTER_NEWDATA && lastVisiblePosition + 1 == mAdapter.getItemCount() && dy > 0) {
                    mPresenter.loadMore();
                }
            }
        });

        final InteractInterface interact = (InteractInterface) getActivity();
        if (interact.getRecycledViewPool() != null) {
            mImageList.setRecycledViewPool(interact.getRecycledViewPool());
        } else {
            interact.setRecycledViewPool(mImageList.getRecycledViewPool());
        }

//        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        //解决瀑布流item跳动的问题
//        manager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        GridLayoutManager manager = new GridLayoutManager(getContext(), 2);
        mImageList.setLayoutManager(manager);

        mAlertBuilder = new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setMessage("您当前为非Wifi环境，是否继续加载图片")
                .setTitle("注意")
                .setNegativeButton("否", null)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        interact.setCanLoadWithoutWifi(true);
                        mPresenter.refresh();
                    }
                });

        mAdapter = new PhotoAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mImageList.setAdapter(mAdapter);
    }

    @Override
    public void showNewData(boolean hasMore, List<Photo> newData) {
        mAdapter.addImages(newData);
        if (hasMore) {
            mAdapter.setFooterState(PhotoAdapter.FOOTER_NEWDATA);
        } else {
            mAdapter.setFooterState(PhotoAdapter.FOOTER_NODATA);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showError() {
        mAdapter.setFooterState(PhotoAdapter.FOOTER_ERROR);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoading() {
        mAdapter.setFooterState(PhotoAdapter.FOOTER_LOADING);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void collectSuccess(String msg) {
        mAlbumInfo.setFavorite(!mAlbumInfo.isFavorite());
        //将isFavorite更新保存到数据库
        mAlbumInfo.save();
        InteractInterface interact = (InteractInterface) getActivity();
        interact.changeCollectState(mAlbumInfo.isFavorite());
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void collectFail(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        mSwipeRefresh.setRefreshing(isRefreshing);
    }

    @Override
    public void showAlertDialog() {
        mAlertBuilder.show();
    }

    @Override
    public void onRefreshFinish() {
        mAdapter.cleanImages();
    }

    @Override
    public Album getAlbumInfo() {
        return mAlbumInfo;
    }

    @Override
    public void onItemClick(View view, int position) {
        mPresenter.openDetail((ArrayList<Photo>) mAdapter.getImages(), position, mAlbumInfo);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AlbumPresenter.SELECT_PHOTO:
                //上传照片成功，刷新相册
                if (resultCode == Activity.RESULT_OK) {
                    mPresenter.refresh();
                }
                break;
            case AlbumPresenter.OPEN_PHOTO_DETAIL:
                if (resultCode == Activity.RESULT_OK) {
                    int index = data.getIntExtra(DetailActivity.CURRENT_INDEX, 0);
                    int currentPage = data.getIntExtra(DetailActivity.CURRENT_PAGE, 0);
                    ArrayList list = (ArrayList) data.getSerializableExtra(DetailActivity.DETAILS_KEY);
                    refreshFromDetail(list, index, currentPage);
                }
                break;
            default:
        }
    }

    private void refreshFromDetail(ArrayList list, int index, int currentPage) {
        mAdapter.cleanImages();
        mAdapter.addImages(list);
        mAdapter.notifyDataSetChanged();

        mImageList.scrollToPosition(index);

        mPresenter.setCurrentPage(currentPage);
    }

    public static AlbumFragment newInstance(Album album) {
        AlbumFragment fragment = new AlbumFragment();
        //api中建议通过setArguments携带参数
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_ALBUM, album);
        fragment.setArguments(bundle);
        return fragment;
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
    public Activity getContactActivity() {
        return getActivity();
    }
}
