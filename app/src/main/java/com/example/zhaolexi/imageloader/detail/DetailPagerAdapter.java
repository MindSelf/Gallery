package com.example.zhaolexi.imageloader.detail;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.imageloader.imageloader.ImageLoader;
import com.example.imageloader.imageloader.TaskOption;
import com.example.imageloader.resizer.DecodeOption;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.utils.DisplayUtils;

import java.util.List;

public class DetailPagerAdapter<V extends Detail> extends PagerAdapter {

    private List<V> mDetailList;

    private static final int REQ_WIDTH = 380;
    private static final int MIN_WIDTH = 380;
    private static final int MIN_HEIGHT = 650;
    private int reqWidth = 380;

    public DetailPagerAdapter(List<V> originList) {
        mDetailList = originList;
        reqWidth = DisplayUtils.getScreenMetrics(BaseApplication.getContext()).widthPixels - DisplayUtils.dp2px(BaseApplication.getContext(), 60);
    }

    @Override
    public int getCount() {
        return mDetailList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        //销毁ImageView后请求gc
        System.gc();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Context context = container.getContext();

        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setMinimumWidth(DisplayUtils.dp2px(context, MIN_WIDTH));
        imageView.setMinimumHeight(DisplayUtils.dp2px(context, MIN_HEIGHT));
        container.addView(imageView);

        Detail detail = mDetailList.get(position);
        DecodeOption decodeOption = new DecodeOption(DisplayUtils.dp2px(BaseApplication.getContext(), REQ_WIDTH), 0);
        decodeOption.shouldResized = detail.shouldResized();
        TaskOption option = new TaskOption(decodeOption);
        option.priority = 1;
        ImageLoader.getInstance(container.getContext()).bindBitmap(detail.getDetailUrl(), imageView, option);
        return imageView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public V getDetailInfo(int position) {
        return mDetailList.get(position);
    }

    public void addDetailInfos(List<V> newDatas) {
        mDetailList.addAll(newDatas);
        notifyDataSetChanged();
    }

    public void removeDetail(int position) {
        mDetailList.remove(position);
        notifyDataSetChanged();
    }
}
