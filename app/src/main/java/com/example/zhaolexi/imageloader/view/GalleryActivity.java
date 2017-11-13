package com.example.zhaolexi.imageloader.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.adapter.ImageAdapter;
import com.example.zhaolexi.imageloader.base.BaseActivity;
import com.example.zhaolexi.imageloader.bean.Image;
import com.example.zhaolexi.imageloader.presenter.ImagePresenter;
import com.example.zhaolexi.imageloader.ui.GridItemTouchHelperCallback;
import com.example.zhaolexi.imageloader.ui.SpacesItemDecoration;
import com.example.zhaolexi.imageloader.utils.MyUtils;

import java.util.List;

public class GalleryActivity extends BaseActivity<ImageViewInterface,ImagePresenter> implements ImageViewInterface, ImageAdapter.OnItemClickListener {

    private RecyclerView mImageList;
    private AlertDialog.Builder builder;
    private ImageAdapter mAdapter;
    private boolean mIsLoading=false;

    @Override
    protected void onStart() {
        super.onStart();
        if(!MyUtils.isWifi(this))
            builder.show();
        else
            mPresenter.loadMore();
    }

    @Override
    protected void onDestroy() {
        System.gc();
        //小米在退出activity时不能立即gc，这时候要是反复启动activity，会导致OOM
        super.onDestroy();
    }

    @Override
    protected ImagePresenter createPresenter() {
        return new ImagePresenter();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_image);

        mImageList = (RecyclerView) findViewById(R.id.recyclerview);
        mImageList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mImageList.addItemDecoration(new SpacesItemDecoration(MyUtils.dp2px(this,4)));
        mImageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE) {
                    mAdapter.setIsIdle(true);
                    //滑动时onBindViewHolder先于回调方法，所以静止时要提醒Adapter更新数据
                    mAdapter.notifyDataSetChanged();
                }else{
                    mAdapter.setIsIdle(false);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int lastVisiblePosition = getLastVisiblePosition();
                if(!mIsLoading&&lastVisiblePosition+1==mAdapter.getItemCount()&&dy>0){
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

        builder = new AlertDialog.Builder(this);
        builder.setMessage("您当前为非Wifi环境，是否继续加载图片");
        builder.setTitle("注意");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.loadMore();
            }
        });
        builder.setNegativeButton("否", null);

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
                int max=lastPositions[0];
                for (int i : lastPositions) {
                    if(i>max)
                        max=i;
                }
                position=max;
            }
            return position;

    }

    @Override
    public void showNewDatas(List<Image> newDatas) {
        mIsLoading = false;
        int startPosition=mAdapter.getItemCount()-1;
        mAdapter.addImages(newDatas);
        mAdapter.setFooterState(ImageAdapter.FOOTER_NEWDATA);
        mAdapter.notifyItemRangeInserted(startPosition,newDatas.size());
    }

    @Override
    public void showError() {
        mIsLoading=false;
        mAdapter.setFooterState(ImageAdapter.FOOTER_ERROR);
        mAdapter.notifyItemChanged(mAdapter.getItemCount()-1);
    }

    @Override
    public void showNoData() {
        mIsLoading=false;
        mAdapter.setFooterState(ImageAdapter.FOOTER_NODATA);
        mAdapter.notifyItemChanged(mAdapter.getItemCount()-1);
    }

    @Override
    public void showLoading() {
        mIsLoading=true;
        mAdapter.setFooterState(ImageAdapter.FOOTER_LOADING);
        mAdapter.notifyItemChanged(mAdapter.getItemCount()-1);
    }

    @Override
    public void onItemClick(View view, int position) {
        String url=mAdapter.getImageList().get(position).getUrl();
        mPresenter.startActivity(url);
    }

}
