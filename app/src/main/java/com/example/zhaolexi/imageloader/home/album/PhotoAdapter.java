package com.example.zhaolexi.imageloader.home.album;


import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.imageloader.imageloader.ImageLoader;
import com.example.imageloader.imageloader.ImageLoaderConfig;
import com.example.imageloader.imageloader.TaskOption;
import com.example.imageloader.resizer.DecodeOption;
import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseFragment;
import com.example.zhaolexi.imageloader.common.ui.OnItemClickListener;
import com.example.zhaolexi.imageloader.common.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public class PhotoAdapter extends RecyclerView.Adapter implements PhotoItemTouchHelperCallback.ItemTouchHelperAdapter {

    private Context mCtx;
    private ImageLoader mImageLoader;
    private AlbumPresenter mPresenter;
    private List<Photo> mPhotoList;
    private OnItemClickListener onItemClickListener;

    //初始值为true，因为我们是在onScrollStateChange时设置idle值，而刚打开时不会回调onScrollStateChange
    private boolean mIsIdle = true;
    //初始为NEWDATA，说明当前没有加载图片
    private int mFooterState = FOOTER_NEWDATA;
    private int mImageWidth;

    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_FOOTER = 2;
    public static final int FOOTER_NEWDATA = 1 << 1;
    public static final int FOOTER_LOADING = 1 << 2;
    public static final int FOOTER_ERROR = 1 << 3;
    public static final int FOOTER_NODATA = 1 << 4;

    public PhotoAdapter(BaseFragment fragment) {
        mCtx = fragment.getContext();
        mPresenter = (AlbumPresenter) fragment.getPresenter();
        mPhotoList = new ArrayList<>();
        ImageLoaderConfig config = new ImageLoaderConfig.Builder(mCtx).setDefaultImage(R.color.windowBackground).build();
        mImageLoader = ImageLoader.getInstance(mCtx);
        mImageLoader.init(config);
        int screenWidth = DisplayUtils.getScreenMetrics(mCtx).widthPixels;
        mImageWidth = (screenWidth - DisplayUtils.dp2px(mCtx, 16)) / 2;
    }

    public void setFooterState(int footerState) {
        mFooterState = footerState;
    }

    public int getFooterState() {
        return mFooterState;
    }

    public void setIsIdle(boolean isIdle) {
        this.mIsIdle = isIdle;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public List<Photo> getImages() {
        return mPhotoList;
    }

    public void cleanImages() {
        mPhotoList.clear();
    }

    public void addImages(List<Photo> newDatas) {
        mPhotoList.addAll(newDatas);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
            lp.setFullSpan(getItemViewType(holder.getLayoutPosition()) == TYPE_FOOTER);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == TYPE_FOOTER ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }


    @Override
    public int getItemViewType(int position) {
        return position + 1 == getItemCount() ? TYPE_FOOTER : TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(mCtx).inflate(R.layout.item_image_list, parent, false);
            return new ItemViewHolder(view);
        } else {
            View mFooter = LayoutInflater.from(mCtx).inflate(R.layout.footer_image_list, parent, false);
            return new FooterViewHolder(mFooter);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            ImageView image = viewHolder.image;
            TextView description = viewHolder.description;
            description.setText(mPhotoList.get(position).getDescription());

            //加载和显示图片
            final String tag = (String) image.getTag();
            final String uri = mPhotoList.get(position).getThumbUrl();

            TaskOption option = new TaskOption(new DecodeOption(mImageWidth, mImageWidth));
            //NOTE: 由于RecyclerView的复用机制，即使同个position的viewHolder也是不断变化的，所以导致isNeedLoad始终为true
            boolean isNeedLoad = uri != null && !uri.equals(tag);
            if (isNeedLoad) {
                //为了避免View复用导致显示旧的bitmap，这里会先显示内存中缓存的图片，没有再显示占位图
                mImageLoader.bindDefaultImage(uri, image, option);
            }

            //优化列表卡顿，为了避免频繁的加载图片，只在列表停下来的时候才加载图片
            if (isNeedLoad && mIsIdle) {
                image.setTag(uri);
                mImageLoader.bindBitmap(uri, image, option);
            }

        } else {
            FooterViewHolder viewHolder = (FooterViewHolder) holder;
            switch (mFooterState) {
                case FOOTER_NEWDATA:
                    //当前没有加载图片，隐藏footer
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.textView.setVisibility(View.GONE);
                    break;
                case FOOTER_LOADING:
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    viewHolder.textView.setVisibility(View.VISIBLE);
                    viewHolder.textView.setText(R.string.footer_loading);
                    break;
                case FOOTER_ERROR:
                    viewHolder.progressBar.setVisibility(View.GONE);
                    TextView textView = viewHolder.textView;
                    textView.setVisibility(View.VISIBLE);
                    SpannableString ss = new SpannableString(
                            mCtx.getResources().getText(R.string.footer_error));
                    ss.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            //重新加载
                            mPresenter.loadMore();
                        }
                    }, 5, 9, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    textView.setText(ss);
                    //为textView设置链接
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    break;
                case FOOTER_NODATA:
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.textView.setVisibility(View.VISIBLE);
                    viewHolder.textView.setText(R.string.footer_nodata);
                    break;
                default:
            }
        }
    }

    @Override
    public int getItemCount() {
        //包括footer
        return mPhotoList.size() + 1;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mPhotoList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public Photo getItem(int position) {
        return mPhotoList.get(position);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        TextView description;

        ItemViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.iv_image);
            description = (TextView) itemView.findViewById(R.id.description);
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getLayoutPosition());
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ProgressBar progressBar;

        FooterViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.footer_tv);
            progressBar = (ProgressBar) itemView.findViewById(R.id.footer_pb);
        }
    }

}
