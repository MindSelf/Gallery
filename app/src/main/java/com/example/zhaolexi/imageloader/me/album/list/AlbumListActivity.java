package com.example.zhaolexi.imageloader.me.album.list;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseActivity;
import com.example.zhaolexi.imageloader.common.ui.OnItemAddListener;
import com.example.zhaolexi.imageloader.common.ui.OnItemClickListener;
import com.example.zhaolexi.imageloader.common.ui.OnItemLongClickListener;
import com.example.zhaolexi.imageloader.common.ui.SpacesItemDecoration;
import com.example.zhaolexi.imageloader.common.utils.DisplayUtils;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.me.album.info.AlbumInfoActivity;
import com.example.zhaolexi.imageloader.me.album.list.favorite.FavoriteAlbumModelImpl;
import com.example.zhaolexi.imageloader.me.album.list.my.MyAlbumModelImpl;

import java.util.ArrayList;
import java.util.List;

public class AlbumListActivity extends BaseActivity<AlbumListPresenter> implements AlbumListViewInterface, OnItemClickListener, OnItemAddListener, AlbumAdapter.OnItemCloseListener, OnItemLongClickListener {

    public static final int REQUEST_INFO = 0;

    public static final String KEY_TYPE = "type";
    public static final String KEY_ALBUM = "album";
    public static final int TYPE_MY = 1;
    public static final int TYPE_FAVORITE = 2;
    private int mType;

    private int mAlbumPos;
    private List<Album> mAlbumList;
    private AlbumAdapter mAdapter;

    private MenuItem mMenuItem;
    private RecyclerView mRecyclerView;

    @Override
    protected void initData() {
        //获取预加载数据
        mAlbumList = (List<Album>) getIntent().getSerializableExtra(KEY_ALBUM);
        if (mAlbumList == null) {
            mAlbumList = new ArrayList<>();
        }

        mType = getIntent().getIntExtra(KEY_TYPE, -1);
        if (mType == TYPE_MY) {
            mPresenter.setModel(new MyAlbumModelImpl());
            mAdapter = new AlbumAdapter(this, mAlbumList, true);
        } else if (mType == TYPE_FAVORITE) {
            mPresenter.setModel(new FavoriteAlbumModelImpl());
            mAdapter = new AlbumAdapter(this, mAlbumList, false);
        }

        if (mAlbumList.isEmpty()) {
            mPresenter.refresh();
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_album_list);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView mTitle = (TextView) findViewById(R.id.tv_title);
        if (mType == TYPE_MY) {
            mTitle.setText(R.string.my_album);
        } else if (mType == TYPE_FAVORITE) {
            mTitle.setText(R.string.my_favorite);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_album_list);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(DisplayUtils.dp2px(this, 4)));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if (lastVisiblePosition == mAdapter.getItemCount() && dy > 0) {
                    mPresenter.loadMore();
                }
            }
        });

        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemAddListener(this);
        mAdapter.setOnItemCloseListener(this);
        mAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

    }

    private int getLastVisiblePosition() {
        int position = 0;
        if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
        } else if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
        } else if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
            //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
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
    protected AlbumListPresenter createPresenter() {
        return new AlbumListPresenter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_album_list, menu);
        mMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.setting) {
            if (mAdapter.isEditable()) {
                mAdapter.setEditable(false);
                mMenuItem.setIcon(R.mipmap.ic_settings);
            } else {
                mAdapter.setEditable(true);
                mMenuItem.setIcon(R.mipmap.ic_done_white);
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INFO && resultCode == RESULT_OK) {
            Album album = (Album) data.getSerializableExtra(AlbumInfoActivity.RETURN);
            if (album != null && mAlbumPos != -1) {
                mAdapter.replaceAlbum(mAlbumPos, album);
                mAlbumPos = -1;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRefreshFinish() {
        mAdapter.clearAlbums();
    }

    @Override
    public void showNewData(List<Album> newData) {
        mAdapter.addAlbums(newData);
    }

    @Override
    public void showError(String hint) {
        Toast.makeText(this, "加载数据失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCloseSuccess(int pos, int type) {
        switch (type) {
            case TYPE_MY:
                Toast.makeText(this, "删除相册成功", Toast.LENGTH_SHORT).show();
                break;
            case TYPE_FAVORITE:
                Toast.makeText(this, "取消收藏", Toast.LENGTH_SHORT).show();
                break;
        }
        mAdapter.removeAlbum(pos);
    }

    @Override
    public void onCloseFail(String hint) {
        Toast.makeText(this, hint, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Activity getContactActivity() {
        return this;
    }

    @Override
    public void onItemClick(View view, int position) {
        mAlbumPos = position;
        mPresenter.openAlbumInfo(mAlbumList.get(position), mType);
    }

    @Override
    public void onClickItemAdd() {

    }

    @Override
    public void onItemClose(View v, int position) {
        mPresenter.closeAlbum(mAlbumList.get(position), position, mType);
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        mAdapter.setEditable(true);
        mMenuItem.setIcon(R.mipmap.ic_done_white);
        return true;
    }
}
