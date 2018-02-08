package com.example.zhaolexi.imageloader.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.base.BaseFragment;
import com.example.zhaolexi.imageloader.bean.Image;
import com.example.zhaolexi.imageloader.callback.OnItemClickListener;
import com.example.zhaolexi.imageloader.presenter.AlbumPresenter;
import com.example.zhaolexi.imageloader.ui.ImageItemTouchHelperCallback;
import com.example.zhaolexi.imageloader.utils.MyUtils;
import com.example.zhaolexi.imageloader.utils.loader.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public class ImageAdapter extends RecyclerView.Adapter implements ImageItemTouchHelperCallback.ItemTouchHelperAdapter {

    private List<Image> mImageList;
    //记录图片高度
    //    private SparseIntArray mHeightsArray;
    private ImageLoader mImageLoader;
    private Context mCtx;
    private AlbumPresenter mPresenter;
    private Drawable mDefaultBitmapDrawable;
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

    public ImageAdapter(BaseFragment fragment) {
        mCtx = fragment.getContext();
        mPresenter = (AlbumPresenter) fragment.getPresenter();
        mImageList = new ArrayList<>();
        //        mHeightsArray = new SparseIntArray();
        mImageLoader = new ImageLoader.Builder(mCtx).initMemoryCacheRate(4).build();
        mDefaultBitmapDrawable = mCtx.getResources().getDrawable(R.color.windowBackground);
        int screenWidth = MyUtils.getScreenMetrics(mCtx).widthPixels;
        mImageWidth = (screenWidth - MyUtils.dp2px(mCtx, 16)) / 2;
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

    public void cleanImages() {
        mImageList.clear();
    }

    public void addImages(List<Image> newDatas) {
        mImageList.addAll(newDatas);
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
            ImageView imageView = viewHolder.imageView;
            TextView textView = viewHolder.description;
            textView.setText(mImageList.get(position).getDescription());

            //加载和显示图片
            final String tag = (String) imageView.getTag();
            final String uri = mImageList.get(position).getThumbUrl();

            if (!uri.equals(tag)) {
                //为了避免View复用导致显示旧的bitmap，这里会先显示内存中缓存的图片，没有再显示占位图
                Bitmap bitmap = mImageLoader.loadBitmapFromMemCache(uri);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
//                else if (mHeightsArray.get(position) != 0) {
//                    //如果是之前显示过的图片，就更改占位图的大小
//                    Bitmap tBitmap = Bitmap.createBitmap(mImageWidth, mHeightsArray.get(position), Bitmap.Config.RGB_565);
//                    Canvas canvas = new Canvas(tBitmap);
//                    mDefaultBitmapDrawable.setBounds(0, 0, mImageWidth, mHeightsArray.get(position));
//                    mDefaultBitmapDrawable.draw(canvas);
//                    imageView.setImageBitmap(tBitmap);
//                }
                else {
                    imageView.setImageDrawable(mDefaultBitmapDrawable);
                }
            }
            //优化列表卡顿，为了避免频繁的加载图片，只在列表停下来的时候才加载图片
            if (mIsIdle) {
                imageView.setTag(uri);
                ImageLoader.TaskOptions options = new ImageLoader.TaskOptions(mImageWidth, 0);
//                options.onLoadBitmapListener = new ImageLoader.TaskOptions.OnLoadBitmapListener() {
//                    @Override
//                    public void onFinish(Bitmap bitmap) {
//                        if (mHeightsArray.get(position) == 0) {
//                            //记录图片高度
//                            mHeightsArray.put(position, bitmap.getHeight());
//                        }
//                    }
//                };
                mImageLoader.bindBitmap(uri, imageView, options);
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

        ItemViewHolder(View itemView) {
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

        FooterViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.footer_tv);
            progressBar = (ProgressBar) itemView.findViewById(R.id.footer_pb);
        }
    }

}
