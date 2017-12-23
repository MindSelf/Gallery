package com.example.zhaolexi.imageloader.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.bean.Image;
import com.example.zhaolexi.imageloader.ui.GridItemTouchHelperCallback;
import com.example.zhaolexi.imageloader.utils.MyUtils;
import com.example.zhaolexi.imageloader.utils.loader.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public class ImageAdapter extends RecyclerView.Adapter implements GridItemTouchHelperCallback.ItemTouchHelperAdapter {

    private List<Image> mImageList;

    private ImageLoader mImageLoader;

    private Drawable mDefaultBitmapDrawable;
    private View mFooter;
    private OnItemClickListener onItemClickListener;

    //初始值为true，因为我们是在onScrollStateChange时设置idle值，而刚打开时不会回调onScrollStateChange
    private boolean mIsIdle = true;
    private int mFooterState;
    private int mImageWidth;

    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_FOOTER = 2;
    public static final int FOOTER_LOADING = 1 << 1;
    public static final int FOOTER_NEWDATA = 1 << 2;
    public static final int FOOTER_ERROR = 1 << 3;
    public static final int FOOTER_NODATA = 1 << 4;

    public ImageAdapter(Context context) {
        mImageList = new ArrayList<>();
        mImageLoader = ImageLoader.Builder.build(context);
        mDefaultBitmapDrawable = context.getResources().getDrawable(R.drawable.image_default);
        int screenWidth = MyUtils.getScreenMetrics(context).widthPixels;
        mImageWidth = (screenWidth - MyUtils.dp2px(context, 16)) / 2;
    }

    public void setFooterState(int footerState) {
        mFooterState = footerState;
    }

    public void setIsIdle(boolean isIdle) {
        this.mIsIdle = isIdle;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void clearImages() {
        mImageList.clear();
        notifyDataSetChanged();
    }

    public void addImages(List<Image> newDatas) {
        mImageList.addAll(newDatas);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
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
        int type = position + 1 == getItemCount() ? TYPE_FOOTER : TYPE_NORMAL;
        return type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list_item, parent, false);
            return new ItemViewHolder(view);
        } else {
            mFooter = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list_footer, parent, false);
            return new FooterViewHolder(mFooter);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            ItemViewHolder viewholder = (ItemViewHolder) holder;
            ImageView imageView = viewholder.imageView;
            TextView textView = viewholder.description;
            textView.setText(mImageList.get(position).getDescription());

            final String tag = (String) imageView.getTag();
            final String uri = mImageList.get(position).getThumbUrl();

            if (!uri.equals(tag)) {
                //为了避免View复用导致显示旧的bitmap，这里会先显示内存中缓存的图片，没有再显示占位图
                Bitmap bitmap = mImageLoader.loadBitmapFromMemCache(uri);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageDrawable(mDefaultBitmapDrawable);
                }
                bitmap = null;
            }
            //优化列表卡顿，为了避免频繁的加载图片，只在列表停下来的时候才加载图片
            if (mIsIdle) {
                imageView.setTag(uri);
                mImageLoader.bindBitmap(uri, imageView, new ImageLoader.TaskOptions(mImageWidth, 0));
            }

        } else {
            FooterViewHolder viewHolder = (FooterViewHolder) holder;
            switch (mFooterState) {
                case FOOTER_NEWDATA:
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.textView.setVisibility(View.GONE);
                    break;
                case FOOTER_LOADING:
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    viewHolder.textView.setVisibility(View.VISIBLE);
                    viewHolder.textView.setText("正在加载");
                    break;
                case FOOTER_ERROR:
                    break;
                case FOOTER_NODATA:
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.textView.setVisibility(View.VISIBLE);
                    viewHolder.textView.setText("没有更多图片了");
                    break;
                default:
            }
        }
    }

    @Override
    public int getItemCount() {
        return mImageList.size() + 1;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mImageList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public Image getItem(int position) {
        return mImageList.get(position);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView;
        TextView description;

        public ItemViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.iv_image);
            description = (TextView) itemView.findViewById(R.id.description);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getLayoutPosition());
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ProgressBar progressBar;

        public FooterViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.footer_tv);
            progressBar = (ProgressBar) itemView.findViewById(R.id.footer_pb);
        }
    }

}
