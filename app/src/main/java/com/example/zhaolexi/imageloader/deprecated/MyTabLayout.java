package com.example.zhaolexi.imageloader.deprecated;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.example.zhaolexi.imageloader.R;

/**
 * Created by ZHAOLEXI on 2018/1/30.
 */

@Deprecated
public class MyTabLayout extends TabLayout {

    private OnTabClickListener mOnTabClickListener;
    private boolean mIsTabClosable;

    public MyTabLayout(Context context) {
        super(context);
    }

    public MyTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*
    PagerAdapter刷新数据时会调用PagerAdapterObserver.onChanged来通知数据已经被更新
    原TabLayout在获知数据更新后会调用populateFromPagerAdapter方法，在该方法中removeAllTabs，因此自己添加的Tab会被移除
    不过当Tab移除后会调用newTab得到一个Tab来构建新的TabView，所以可以通过重写该方法使新建的TabView仍使用自定义的布局
     */
    @NonNull
    @Override
    public Tab newTab() {
        Tab tab=super.newTab();
        final int index=getTabCount();

        tab.setCustomView(R.layout.custom_tab);
        View custom=tab.getCustomView();
        ImageView icon = (ImageView) custom.findViewById(R.id.iv_close);
        icon.setVisibility(mIsTabClosable?VISIBLE:GONE);
        custom.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setTabClosable(true);
                return mOnTabClickListener.onLongClick(index);
            }
        });
        icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTabClickListener.onClose(index);
            }
        });
        return tab;
    }

    public void setTabClosable(boolean closable) {
        mIsTabClosable=closable;
        int count=getTabCount();
        for(int i=0;i<count;i++) {
            ImageView icon = (ImageView) getTabAt(i).getCustomView().findViewById(R.id.iv_close);
            icon.setVisibility(closable?VISIBLE:GONE);
        }
    }

    public void setOnTabCloseListener(OnTabClickListener listener) {
        mOnTabClickListener =listener;
    }

    public interface OnTabClickListener {
        void onClose(int position);

        boolean onLongClick(int position);
    }
}
