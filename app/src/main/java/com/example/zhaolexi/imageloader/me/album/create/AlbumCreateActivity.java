package com.example.zhaolexi.imageloader.me.album.create;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseActivity;
import com.example.zhaolexi.imageloader.common.utils.CompoundDrawableUtils;
import com.example.zhaolexi.imageloader.home.manager.Album;

public class AlbumCreateActivity extends BaseActivity<AlbumCreatePresenter> implements AlbumCreateViewInterface, RadioGroup.OnCheckedChangeListener, View.OnTouchListener, View.OnFocusChangeListener, View.OnClickListener {

    public static final String KEY_RETURN = "return";

    private EditText mTitle, mDescription;
    private EditText mReadPassword, mModPassword;
    private RadioGroup mRadioGroup;
    private RelativeLayout mPasswordLayout;
    private TextView mCreate;

    public static final int CLOSE_ICON_SIZE = 40;
    public static final int PASSWORD_ICON_SIZE = 37;
    private boolean mHasTouchDrawable;
    private boolean mIsClearShown;

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_create_album);

        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitle = (EditText) findViewById(R.id.et_title);
        mTitle.setOnTouchListener(this);
        mTitle.setOnFocusChangeListener(this);
        mTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s) && !mIsClearShown) {
                    showTitleClear();
                } else if (TextUtils.isEmpty(s) && mIsClearShown) {
                    dismissTitleClear();
                }
            }
        });

        mDescription = (EditText) findViewById(R.id.et_description);

        mReadPassword = (EditText) findViewById(R.id.et_read_password);
        mReadPassword.setOnTouchListener(this);
        CompoundDrawableUtils.setPasswordInvisible(mReadPassword, R.mipmap.ic_visibility_off_grey, PASSWORD_ICON_SIZE, PASSWORD_ICON_SIZE);
        mModPassword = (EditText) findViewById(R.id.et_mod_password);
        mModPassword.setOnTouchListener(this);
        CompoundDrawableUtils.setPasswordInvisible(mModPassword, R.mipmap.ic_visibility_off_grey, PASSWORD_ICON_SIZE, PASSWORD_ICON_SIZE);

        mRadioGroup = (RadioGroup) findViewById(R.id.rg_permission);
        mRadioGroup.setOnCheckedChangeListener(this);
        mPasswordLayout = (RelativeLayout) findViewById(R.id.rl_password);

        mCreate = (TextView) findViewById(R.id.tv_create);
        mCreate.setOnClickListener(this);
    }

    @Override
    protected AlbumCreatePresenter createPresenter() {
        return new AlbumCreatePresenter();
    }

    @Override
    public Activity getContactActivity() {
        return this;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_public:
                mPasswordLayout.setVisibility(View.GONE);
                mReadPassword.setText("");
                mModPassword.setText("");
                CompoundDrawableUtils.setPasswordInvisible(mReadPassword, R.mipmap.ic_visibility_off_grey, PASSWORD_ICON_SIZE, PASSWORD_ICON_SIZE);
                CompoundDrawableUtils.setPasswordInvisible(mModPassword, R.mipmap.ic_visibility_off_grey, PASSWORD_ICON_SIZE, PASSWORD_ICON_SIZE);
                break;
            case R.id.rb_private:
                mPasswordLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHasTouchDrawable = CompoundDrawableUtils.isTouchWithinDrawable(v, event);
                break;
            case MotionEvent.ACTION_UP:
                if (mHasTouchDrawable && CompoundDrawableUtils.isTouchWithinDrawable(v, event)) {
                    switch (v.getId()) {
                        case R.id.et_title:
                            ((EditText) v).setText("");
                            break;
                        case R.id.et_read_password:
                        case R.id.et_mod_password:
                            EditText editText = (EditText) v;
                            if (CompoundDrawableUtils.isPasswordVisible(editText)) {
                                CompoundDrawableUtils.setPasswordInvisible(editText, R.mipmap.ic_visibility_off_grey, PASSWORD_ICON_SIZE, PASSWORD_ICON_SIZE);
                            } else {
                                CompoundDrawableUtils.setPasswordVisible(editText, R.mipmap.ic_visibility_grey, PASSWORD_ICON_SIZE, PASSWORD_ICON_SIZE);
                            }
                            break;
                    }
                }
                break;
        }
        return false;
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_title:
                if (!mIsClearShown && hasFocus && !TextUtils.isEmpty(mTitle.getText())) {
                    showTitleClear();
                } else if (mIsClearShown && !hasFocus) {
                    dismissTitleClear();
                }
                break;
        }
    }

    private void showTitleClear() {
        CompoundDrawableUtils.showEditDrawable(mTitle, R.drawable.bg_close_icon, CLOSE_ICON_SIZE, CLOSE_ICON_SIZE);
        mIsClearShown = true;
    }

    private void dismissTitleClear() {
        CompoundDrawableUtils.dismissEditDrawable(mTitle);
        mIsClearShown = false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_create) {
            if (mRadioGroup.getCheckedRadioButtonId() == R.id.rb_private &&
                    TextUtils.isEmpty(mReadPassword.getText()) && TextUtils.isEmpty(mModPassword.getText())) {
                Toast.makeText(this, "私密相册密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            mPresenter.createAlbum(mTitle.getText().toString(), mDescription.getText().toString(),
                    mReadPassword.getText().toString(), mModPassword.getText().toString());
        }
    }

    @Override
    public void onCreateSuccess(Album album) {
        Toast.makeText(this, "创建成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra(KEY_RETURN, album);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCreateFail(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
    }
}
