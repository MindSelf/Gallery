package com.example.zhaolexi.imageloader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.base.MyApplication;
import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.utils.MyUtils;
import com.example.zhaolexi.imageloader.utils.loader.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by ZHAOLEXI on 2017/11/18.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    public static final int MAX_SIZE = 4;
    private List<Photo> mDatas;
    private Set<Integer> mSelected;
    private ImageLoader mImageLoader;
    private Drawable mDefaultDrawable;

    private OnItemClickListener mOnItemClickListener;
    private OnSelectCountChangeListner mOnSelectCountChangeListener;

    private int mEdge;
    private boolean mIsIdle = true;

    public PhotoAdapter(Context context) {
        mDatas = new ArrayList<>();
        mSelected = new HashSet<>(MAX_SIZE);
        mImageLoader = ImageLoader.Builder.build(MyApplication.getContext());
        mDefaultDrawable = context.getResources().getDrawable(R.mipmap.image_default);
        int screenWidth = MyUtils.getScreenMetrics(context).widthPixels;
        mEdge = (screenWidth - MyUtils.dp2px(context, 6)) / 3;
    }

    public void setDatas(List<Photo> newDatas) {
        mDatas.clear();
        mDatas.addAll(newDatas);
    }

    public void setIsIdle(boolean isIdle) {
        this.mIsIdle = isIdle;
    }

    public void setOnItemClickListner(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setSelectCountChangeListener(OnSelectCountChangeListner selectCountChangeListener) {
        this.mOnSelectCountChangeListener = selectCountChangeListener;
    }

    public List<File> getSelectedPhotos() {
        List<File> list = new ArrayList<>();
        Iterator<Integer> iterator = mSelected.iterator();
        while (iterator.hasNext()) {
            File file = new File(mDatas.get(iterator.next()).getPath());
            list.add(file);
        }
        return list;
    }

    public int getSelectedCount() {
        return mSelected.size();
    }

    public void clearSelectedSet() {
        mSelected.clear();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PhotoAdapter.ViewHolder holder, final int position) {
        final ImageView image = holder.iv_image;
        final ImageView block = holder.iv_block;
        final CheckBox checkBox = holder.checkBox;


        if (mSelected.contains(position) && !checkBox.isChecked()) {
            //先清空监听器，防止对setChecked结果产生干扰
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(true);
            block.setSelected(true);
        } else if (!mSelected.contains(position) && checkBox.isChecked()) {
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(false);
            block.setSelected(false);
        }

        //设置监听器
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, holder.getLayoutPosition());
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSelected.size() >= MAX_SIZE && isChecked) {
                    checkBox.setChecked(false);
                    mOnSelectCountChangeListener.onOverSelect();
                } else if (isChecked) {
                    block.setSelected(true);
                    mSelected.add(position);
                    mOnSelectCountChangeListener.onSelectedCountChange(mSelected.size());
                } else {
                    block.setSelected(false);
                    mSelected.remove(position);
                    mOnSelectCountChangeListener.onSelectedCountChange(mSelected.size());
                }
            }
        });

        String tag = (String) image.getTag();
        String uri = mDatas.get(position).getThumbnailPath();
        if (uri == null) {
            uri = mDatas.get(position).getPath();
        }

        if (!uri.equals(tag)) {
            //为了避免View复用导致显示旧的bitmap，这里会先显示内存中缓存的图片，没有再显示占位图
            Bitmap bitmap = mImageLoader.loadBitmapFromMemCache(uri);
            if (bitmap != null) {
                image.setImageBitmap(bitmap);
            } else {
                image.setImageDrawable(mDefaultDrawable);
            }
            bitmap = null;
        }
        //优化列表卡顿，为了避免频繁的加载图片，只在列表停下来的时候才加载图片
        if (mIsIdle) {
            image.setTag(uri);
            mImageLoader.bindBitmap(uri, image, new ImageLoader.TaskOptions(mEdge, mEdge));
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public Photo getItem(int position) {
        return mDatas.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_image, iv_block;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_image = (ImageView) itemView.findViewById(R.id.iv_image);
            iv_block = (ImageView) itemView.findViewById(R.id.iv_block);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_select);
        }
    }

    public interface OnSelectCountChangeListner {

        void onSelectedCountChange(int size);

        void onOverSelect();
    }
}
