package com.example.zhaolexi.imageloader.view;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.adapter.SelectPhotoAdapter;
import com.example.zhaolexi.imageloader.base.BasePresenter;
import com.example.zhaolexi.imageloader.presenter.DetailPresenter;

import java.util.HashSet;
import java.util.Locale;

public class LocalDetailActivity extends DetailActivity {

    public static final String SELECT_SET = "select";

    private HashSet<Integer> mSelectSet;
    private CheckBox checkBox;

    @Override
    protected BasePresenter createPresenter() {
        return new DetailPresenter();
    }

    @Override
    protected void initData() {
        super.initData();
        Intent intent = getIntent();
        mSelectSet = (HashSet<Integer>) intent.getSerializableExtra(SELECT_SET);
    }

    @Override
    protected void initView() {
        super.initView();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                checkBox.setChecked(mSelectSet.contains(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void setResultIntent(Intent intent) {
        intent.putExtra(SELECT_SET, mSelectSet);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_local_detail, menu);
        checkBox = new CheckBox(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 30, 0);
        checkBox.setLayoutParams(params);
        checkBox.setButtonDrawable(R.drawable.bg_checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !mSelectSet.contains(mViewPager.getCurrentItem())) {
                    addSelect();
                } else if (!isChecked && mSelectSet.contains(mViewPager.getCurrentItem())) {
                    mSelectSet.remove(mViewPager.getCurrentItem());
                }
            }
        });
        checkBox.setChecked(mSelectSet.contains(mViewPager.getCurrentItem()));
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(params1);
        linearLayout.addView(checkBox);
        menu.getItem(0).setActionView(linearLayout);
        return true;
    }

    private void addSelect() {
        if (mSelectSet.size() == SelectPhotoAdapter.MAX_SIZE) {
            Toast.makeText(this, String.format(Locale.CHINA, "最多只能选择%d张照片", SelectPhotoAdapter.MAX_SIZE), Toast.LENGTH_SHORT).show();
            checkBox.setChecked(false);
            return;
        }
        mSelectSet.add(mViewPager.getCurrentItem());
    }

}
