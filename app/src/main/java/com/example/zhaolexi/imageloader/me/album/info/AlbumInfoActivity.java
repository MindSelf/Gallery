package com.example.zhaolexi.imageloader.me.album.info;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseActivity;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.utils.AlbumConstructor;
import com.example.zhaolexi.imageloader.common.utils.ClipboardUtils;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.me.album.info.modify.ModifyInfoActivity;
import com.example.zhaolexi.imageloader.me.album.info.modify.PermissionPasswordDialog;
import com.example.zhaolexi.imageloader.redirect.router.Result;

public class AlbumInfoActivity extends BaseActivity<AlbumInfoPresenter> implements AlbumInfoViewInterface, View.OnClickListener, RadioGroup.OnCheckedChangeListener, OnRequestFinishListener<Album>, DialogInterface.OnCancelListener {

    public static final int REQUEST_MODIFY = 0;
    public static final String ALBUM = "album";
    public static final String MODIFY_MODE = "modify";
    public static final String RETURN = "return";

    private Album mAlbumInfo;
    private boolean mIsInModifyMode, mHasModify, mIsCancel;

    private RelativeLayout mPasswordLayout;
    private ImageView mPasswordNavigation;
    private TextView mTitle, mAccount, mCreateTime, mOwner, mDescription, mPermissionHint;
    private RadioGroup mRadioGroup;
    private PermissionPasswordDialog mDialog;

    @Override
    protected void initData() {
        mAlbumInfo = (Album) getIntent().getSerializableExtra(ALBUM);
        if (mAlbumInfo == null) {
            try {
                throw new AlbumInfoException("album can't be null");
            } catch (AlbumInfoException e) {
                e.printStackTrace();
            }
        }
        mIsInModifyMode = getIntent().getBooleanExtra(MODIFY_MODE, false);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_album_info);
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

        mRadioGroup = (RadioGroup) findViewById(R.id.rg_permission);
        mRadioGroup.check(mAlbumInfo.isPublic() ? R.id.rb_public : R.id.rb_private);
        mRadioGroup.setOnCheckedChangeListener(this);

        RelativeLayout titleLayout = (RelativeLayout) findViewById(R.id.rl_title);
        titleLayout.setOnClickListener(this);
        titleLayout.setClickable(mIsInModifyMode);
        RelativeLayout descriptionLayout = (RelativeLayout) findViewById(R.id.rl_description);
        descriptionLayout.setOnClickListener(this);
        descriptionLayout.setClickable(mIsInModifyMode);
        mPasswordLayout = (RelativeLayout) findViewById(R.id.rl_permission);
        mPasswordLayout.setOnClickListener(this);
        mPasswordLayout.setClickable(mIsInModifyMode);
        mPasswordLayout.setVisibility(mIsInModifyMode ? View.VISIBLE : View.GONE);

        ImageView infoNavigation = (ImageView) titleLayout.findViewById(R.id.iv_navigate_title);
        infoNavigation.setVisibility(mIsInModifyMode ? View.VISIBLE : View.GONE);
        ImageView descriptionNavigation = (ImageView) descriptionLayout.findViewById(R.id.iv_navigate_description);
        descriptionNavigation.setVisibility(mIsInModifyMode ? View.VISIBLE : View.GONE);

        mPasswordNavigation = (ImageView) mPasswordLayout.findViewById(R.id.iv_navigate_password);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mAccount = (TextView) findViewById(R.id.tv_account);
        mCreateTime = (TextView) findViewById(R.id.tv_create_time);
        mOwner = (TextView) findViewById(R.id.tv_who);
        mDescription = (TextView) findViewById(R.id.tv_description);
        mPermissionHint = (TextView) findViewById(R.id.permission_hint);
        invalidateAlbumInfo();

        TextView mEnterAlbum = (TextView) findViewById(R.id.tv_visit);
        mEnterAlbum.setOnClickListener(this);

        mDialog = (PermissionPasswordDialog) new PermissionPasswordDialog.Builder(this, mAlbumInfo)
                .setCallback(this)
                .build();
        mDialog.setOnCancelListener(this);
    }

    @Override
    protected AlbumInfoPresenter createPresenter() {
        return new AlbumInfoPresenter();
    }

    private void invalidateAlbumInfo() {
        mTitle.setText(mAlbumInfo.getTitle());
        mAccount.setText(String.valueOf(mAlbumInfo.getAccount()));
        mCreateTime.setText(mAlbumInfo.getCreateTime());
        mOwner.setText(mAlbumInfo.getWho());
        mDescription.setText(mAlbumInfo.getAdesc());
        mPermissionHint.setText(mAlbumInfo.isPublic() ? R.string.album_hint_private : R.string.album_hint_public);
        mPasswordLayout.setClickable(mIsInModifyMode && !mAlbumInfo.isPublic());
        mPasswordNavigation.setVisibility(mIsInModifyMode && !mAlbumInfo.isPublic() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_album_info, menu);
        MenuItem shareItem = menu.getItem(0);
        if (new AlbumConstructor().isThird(mAlbumInfo)) {
            shareItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            ClipboardUtils.clip(mAlbumInfo.getShare());
            Toast.makeText(this, "已将分享链接复制到粘贴板", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MODIFY && resultCode == RESULT_OK) {
            mAlbumInfo = (Album) data.getSerializableExtra(ModifyInfoActivity.KEY_RETURN);
            invalidateAlbumInfo();
            mHasModify = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        if (mHasModify) {
            Intent result = new Intent();
            result.putExtra(RETURN, mAlbumInfo);
            setResult(RESULT_OK, result);
        }
        super.finish();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO cancel事件会触发一次莫名其妙的check
        if (!mIsCancel) {
            switch (checkedId) {
                case R.id.rb_public:
                    if (!mAlbumInfo.isPublic()) {
                        mPresenter.switchToPublic(mAlbumInfo);
                    }
                    break;
                case R.id.rb_private:
                    if (mAlbumInfo.isPublic()) {
                        mDialog.show();
                    }
                    break;
            }
        }
        mIsCancel = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_title:
                mPresenter.openModifyInfo(mAlbumInfo, ModifyInfoActivity.TYPE_TITLE);
                break;
            case R.id.rl_description:
                mPresenter.openModifyInfo(mAlbumInfo, ModifyInfoActivity.TYPE_DESCRIPTION);
                break;
            case R.id.rl_permission:
                mDialog.show();
                break;
            case R.id.tv_visit:
                mPresenter.visitAlbum(mAlbumInfo);
                break;
        }
    }

    @Override
    public void onSwitchToPublicSuccess() {
        Toast.makeText(AlbumInfoActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
        mAlbumInfo.setPublic(true);
        invalidateAlbumInfo();
    }

    @Override
    public void onSwitchToPublicFail(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
        mRadioGroup.check(R.id.rb_private);
    }

    @Override
    public void onSuccess(Album data) {
        mAlbumInfo.setPublic(false);
        invalidateAlbumInfo();
    }

    @Override
    public void onFail(String reason, Result result) {

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mIsCancel = true;
        if (mAlbumInfo.isPublic()) {
            mRadioGroup.check(R.id.rb_public);
        }
    }

    @Override
    public Activity getContactActivity() {
        return this;
    }

}
