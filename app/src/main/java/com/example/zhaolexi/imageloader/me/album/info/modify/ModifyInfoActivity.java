package com.example.zhaolexi.imageloader.me.album.info.modify;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseActivity;
import com.example.zhaolexi.imageloader.common.utils.CompoundDrawableUtils;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.me.album.info.AlbumInfoException;

public class ModifyInfoActivity extends BaseActivity<ModifyInfoPresenter> implements ModifyInfoViewInterface, View.OnTouchListener {

    public static final int TYPE_TITLE = 0;
    public static final int TYPE_DESCRIPTION = 1;

    public static final String KEY_TYPE = "type";
    public static final String KEY_ALBUM = "album";
    public static final String KEY_RETURN = "return";

    public static final int CLOSE_ICON_SIZE = 40;

    private int mType;
    private String mOrigin;
    private Album mAlbumInfo;
    private boolean mHasTouchDrawable;
    private boolean mHasClearShown;

    private EditText mEditText;
    private MenuItem mMenuItem;

    @Override
    public void initView() {
        setContentView(R.layout.activity_modify_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView mTitle = (TextView) findViewById(R.id.tv_title);
        mEditText = (EditText) findViewById(R.id.et_modify);
        if (mType == TYPE_TITLE) {
            mTitle.setText(R.string.modify_title);
            mOrigin = mAlbumInfo.getTitle();
            mEditText.setText(mOrigin);
        } else if (mType == TYPE_DESCRIPTION) {
            mTitle.setText(R.string.modify_description);
            mOrigin = mAlbumInfo.getAdesc();
            mEditText.setText(mOrigin);
        }

        if (mOrigin.length() > 0) {
            showClearDrawable();
        } else {
            dismissClearDrawable();
        }

        mEditText.setOnTouchListener(this);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(mOrigin)) {
                    mMenuItem.setEnabled(false);
                } else {
                    mMenuItem.setEnabled(true);
                }

                if (!TextUtils.isEmpty(s) && !mHasClearShown) {
                    showClearDrawable();
                } else if (TextUtils.isEmpty(s) && mHasClearShown) {
                    dismissClearDrawable();
                }
            }
        });
    }

    private void showClearDrawable() {
        if (mType == TYPE_TITLE) {
            CompoundDrawableUtils.showEditDrawable(mEditText, R.drawable.bg_close_icon, CLOSE_ICON_SIZE, CLOSE_ICON_SIZE);
            mHasClearShown = true;
        }
    }

    private void dismissClearDrawable() {
        mEditText.setCompoundDrawables(null, null, null, null);
        mHasClearShown = false;
    }

    @Override
    public void initData() {
        mType = getIntent().getIntExtra(KEY_TYPE, -1);
        mAlbumInfo = (Album) getIntent().getSerializableExtra(KEY_ALBUM);
        if (mAlbumInfo == null) {
            try {
                throw new AlbumInfoException("album can't be null");
            } catch (AlbumInfoException e) {
                e.printStackTrace();
            }
        }
        mPresenter.initUri(mAlbumInfo, mType);
    }

    @Override
    protected ModifyInfoPresenter createPresenter() {
        return new ModifyInfoPresenter();
    }

    @Override
    public Activity getContactActivity() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_modify, menu);
        mMenuItem = menu.getItem(0);
        mMenuItem.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.finish) {
            if (mPresenter.modify(mType, mEditText.getText().toString())) {
                mEditText.setEnabled(false);
                mMenuItem.setEnabled(false);
            }
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHasTouchDrawable = CompoundDrawableUtils.isTouchWithinDrawable(v, event);
                break;
            case MotionEvent.ACTION_UP:
                if (mHasTouchDrawable && CompoundDrawableUtils.isTouchWithinDrawable(v, event) && v.getId() == R.id.et_modify) {
                    ((EditText) v).setText("");
                }
                break;
        }
        return false;
    }

    @Override
    public void onModifySuccess() {
        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        mEditText.setEnabled(true);
        mMenuItem.setEnabled(true);
        if (mType == TYPE_TITLE) {
            mAlbumInfo.setTitle(mEditText.getText().toString());
        } else if (mType == TYPE_DESCRIPTION) {
            mAlbumInfo.setAdesc(mEditText.getText().toString());
        }
        Intent intent = new Intent();
        intent.putExtra(KEY_RETURN, mAlbumInfo);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onModifyFail(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
        mEditText.setEnabled(true);
        mMenuItem.setEnabled(true);
    }
}
