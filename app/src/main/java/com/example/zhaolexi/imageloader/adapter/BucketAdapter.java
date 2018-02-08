package com.example.zhaolexi.imageloader.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.bean.PhotoBucket;

import java.util.List;

/**
 * Created by ZHAOLEXI on 2017/11/17.
 */

public class BucketAdapter extends BaseAdapter {

    private List<PhotoBucket> mData;

    public BucketAdapter(List<PhotoBucket> list) {
        mData = list;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public PhotoBucket getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bucket_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.iv_cover = (ImageView) view.findViewById(R.id.iv_cover);
            viewHolder.tv_name = (TextView) view.findViewById(R.id.name);
            viewHolder.tv_count = (TextView) view.findViewById(R.id.count);
            viewHolder.radioButton = (RadioButton) view.findViewById(R.id.radio_button);
            view.setTag(viewHolder);
            convertView = view;
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.iv_cover.setImageBitmap(mData.get(position).getCover());
        viewHolder.tv_name.setText(mData.get(position).getName());
        viewHolder.tv_count.setText(String.format("%d张", mData.get(position).getCount()));
        viewHolder.radioButton.setChecked(position == ((ListView) parent).getCheckedItemPosition());
        return convertView;
    }

    class ViewHolder {
        ImageView iv_cover;
        TextView tv_name;
        TextView tv_count;
        RadioButton radioButton;
    }
}
