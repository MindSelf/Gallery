package com.example.zhaolexi.imageloader.me.info;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.utils.SharePreferencesUtils;
import com.example.zhaolexi.imageloader.home.gallery.GalleryActivity;
import com.example.zhaolexi.imageloader.redirect.login.TokenManager;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TokenManager mTokenManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }

    private void initData() {
        mTokenManager = new TokenManager();
    }

    private void initView() {
        setContentView(R.layout.activity_userinfo);
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

        TextView mName = (TextView) findViewById(R.id.tv_name);
        mName.setText(SharePreferencesUtils.getString(SharePreferencesUtils.USER_NAME, ""));
        TextView mMobile = (TextView) findViewById(R.id.tv_mobile);
        mMobile.setText(SharePreferencesUtils.getString(SharePreferencesUtils.MOBILE, ""));
        RelativeLayout mModifyPassword = (RelativeLayout) findViewById(R.id.rl_modify_password);
        mModifyPassword.setOnClickListener(this);
        TextView mLogOut = (TextView) findViewById(R.id.tv_log_out);
        mLogOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_modify_password:
                new ModifyPasswordDialog.Builder(this).build().show();
                break;
            case R.id.tv_log_out:
                mTokenManager.logout();
                Intent intent = new Intent(this, GalleryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
    }
}
