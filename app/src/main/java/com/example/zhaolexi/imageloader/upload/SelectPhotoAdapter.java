package com.example.zhaolexi.imageloader.upload;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.imageloader.imageloader.ImageLoader;
import com.example.imageloader.imageloader.ImageLoaderConfig;
import com.example.imageloader.imageloader.TaskOption;
import com.example.imageloader.resizer.DecodeOption;
import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.ui.OnItemClickListener;
import com.example.zhaolexi.imageloader.common.ui.SquareImageView;
import com.example.zhaolexi.imageloader.common.utils.DisplayUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by ZHAOLEXI on 2017/11/18.
 */

public class SelectPhotoAdapter extends RecyclerView.Adapter<SelectPhotoAdapter.ViewHolder> {

    public static final int MAX_SIZE = 4;
    private List<LocalPhoto> mData;
    private Set<Integer> mSelected;
    private ImageLoader mImageLoader;
    private String mCurrentDate;

    private OnItemClickListener mOnItemClickListener;
    private OnSelectCountChangeListener mOnSelectCountChangeListener;
    private OnDateChangedListener mOnDateChangedListener;

    private int mEdge;
    private boolean mIsIdle = true;
    private boolean mIsUploading = false;

    public SelectPhotoAdapter(Context context) {
        mData = new ArrayList<>();
        mSelected = new HashSet<>(MAX_SIZE);
        ImageLoaderConfig config = new ImageLoaderConfig.Builder(context).build();
        mImageLoader = ImageLoader.getInstance(context);
        mImageLoader.init(config);
        int screenWidth = DisplayUtils.getScreenMetrics(context).widthPixels;
        mEdge = (screenWidth - DisplayUtils.dp2px(context, 6)) / 4;
    }

    public List<LocalPhoto> getList() {
        return mData;
    }

    public Set<Integer> getSelected() {
        return mSelected;
    }

    public void setData(Collection<LocalPhoto> newData) {
        mData.clear();
        mData.addAll(newData);  //将TreeSet里的集合按遍历顺序存放到ArrayList中
        notifyDataSetChanged();
    }

    public void setSelected(Collection<Integer> selected) {
        mSelected.clear();
        mSelected.addAll(selected);
        mOnSelectCountChangeListener.onSelectedCountChange(mSelected.size());
        notifyDataSetChanged();
    }

    public void setIsUploading(boolean isUploading) {
        this.mIsUploading = isUploading;
    }

    public void setIsIdle(boolean isIdle) {
        this.mIsIdle = isIdle;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setSelectCountChangeListener(OnSelectCountChangeListener selectCountChangeListener) {
        this.mOnSelectCountChangeListener = selectCountChangeListener;
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        this.mOnDateChangedListener = onDateChangedListener;
    }

    public List<File> getSelectedPhotos() {
        List<File> list = new ArrayList<>();
        Iterator<Integer> iterator = mSelected.iterator();
        while (iterator.hasNext()) {
            File file = new File(mData.get(iterator.next()).getPath());
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SelectPhotoAdapter.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final SquareImageView image = holder.iv_image;
        final SquareImageView block = holder.iv_block;
        final CheckBox checkBox = holder.checkBox;

        //上传的时候不允许交互操作
        checkBox.setClickable(!mIsUploading);
        if (mSelected.contains(position) && !checkBox.isChecked()) {
            checkBox.setOnCheckedChangeListener(null);  //先清空监听器，防止对setChecked结果产生干扰
            checkBox.setChecked(true);
            block.setVisibility(View.VISIBLE);
        } else if (!mSelected.contains(position) && checkBox.isChecked()) {
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(false);
            block.setVisibility(View.GONE);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mSelected.size() >= MAX_SIZE && isChecked) {
                    checkBox.setChecked(false);
                    mOnSelectCountChangeListener.onOverSelect();
                } else if (isChecked) {
                    block.setVisibility(View.VISIBLE);
                    mSelected.add(position);
                    mOnSelectCountChangeListener.onSelectedCountChange(mSelected.size());
                } else {
                    block.setVisibility(View.GONE);
                    mSelected.remove(position);
                    mOnSelectCountChangeListener.onSelectedCountChange(mSelected.size());
                }
            }
        });

        //设置监听器
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, holder.getLayoutPosition());
            }
        });
        //上传的时候不允许交互操作
        //setOnClickListener会将clickable置为true，所以应该放在setOnClickListener后面
        image.setClickable(!mIsUploading);

        LocalPhoto photo = mData.get(position);
        String tag = (String) image.getTag();
        String uri = photo.getThumbnailPath();
        if (uri == null) {
            uri = photo.getPath();
        }

        TaskOption option = new TaskOption(new DecodeOption(mEdge, mEdge));
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

        DateFormat df = new SimpleDateFormat("yyyy/M", Locale.CHINA);
        String date = df.format(new Date(photo.getLastModified()));
        if (!date.equals(mCurrentDate)) {
            mCurrentDate = date;
            mOnDateChangedListener.onDateChanged(date);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public LocalPhoto getItem(int position) {
        return mData.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        SquareImageView iv_image, iv_block;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            iv_image = (SquareImageView) itemView.findViewById(R.id.iv_image);
            iv_block = (SquareImageView) itemView.findViewById(R.id.photo_block);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_select);
        }
    }

    public interface OnSelectCountChangeListener {

        void onSelectedCountChange(int size);

        void onOverSelect();
    }

    public interface OnDateChangedListener {
        void onDateChanged(String curDate);
    }
}
