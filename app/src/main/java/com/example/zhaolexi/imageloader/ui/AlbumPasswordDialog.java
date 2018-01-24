package com.example.zhaolexi.imageloader.ui;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.base.PasswordDialog;
import com.example.zhaolexi.imageloader.presenter.ImagePresenter;
import com.example.zhaolexi.imageloader.utils.SharePreferencesUtils;
import com.example.zhaolexi.imageloader.utils.Uri;
import com.example.zhaolexi.imageloader.view.GalleryActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ZHAOLEXI on 2017/11/21.
 */

public class AlbumPasswordDialog extends PasswordDialog<AlbumPasswordDialog.AlbumResult> implements View.OnClickListener, TextWatcher, View.OnTouchListener {

    public static final String CREATE_ALBUM = "创建相册";
    public static final String ENTER_ALBUM = "进入相册";

    private String mRandomUrl;
    private String mRandomName;
    private boolean mIsRandom = false;

    private AlbumPasswordDialog(@NonNull Context context) {
        super(context);

        tv_account_name.setText("相册名");
        tv_password_name.setText("相册密码");
        et_account.setHint("请输入相册名称");
        et_password.setHint("请输入相册密码");

        et_account.addTextChangedListener(this);
        et_account.setOnTouchListener(this);
    }

    @Override
    protected AlbumResult newResult() {
        return new AlbumResult();
    }

    @Override
    protected void onHandleData(JSONObject data, AlbumResult result) throws JSONException {
        result.aid = data.getString("aid");
    }

    @Override
    protected boolean checkBeforeRequest() {
        if (mIsRandom) {
            GalleryActivity activity = (GalleryActivity) mCtx;
            ImagePresenter presenter = activity.getPresenter();
            presenter.setUrl(mRandomUrl);
            presenter.refresh();
            SharePreferencesUtils.putString(SharePreferencesUtils.Url, Uri.Girls);
            SharePreferencesUtils.putString(SharePreferencesUtils.Album, "");
            dismiss();
            return false;
        }

        String name = et_account.getText().toString();
        String password = et_password.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
            Toast.makeText(mCtx, "相册名或密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isStringIllegal(name) || isStringIllegal(password)) {
            Toast.makeText(mCtx, "相册名或密码不能包含空格", Toast.LENGTH_SHORT).show();
            return false;
        }

        return super.checkBeforeRequest();
    }

    private boolean isStringIllegal(String str) {
        //不能包含空格
        return str.contains(" ");
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_random:
                //访问服务器获取random的名称和url
                String name = "福利";
                mRandomUrl = Uri.Girls;

                et_account.setTextColor(Color.GRAY);
                //注意setText会触发TextWatcher
                et_account.setText(name);
                et_account.setCursorVisible(false);
                et_password.setText("");
                et_password.setHint("");
                et_account.setSelected(true);
                et_password.setSelected(true);
                et_password.setEnabled(false);
                mIsRandom = true;
                mRandomName = name;
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId()==R.id.et_account_name){
            et_account.setCursorVisible(true);
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //内容经过更改
        if(before!=count) {
            //进入随机浏览时，用户更改了相册名的内容，输入框会变回可编辑状态
            if (mIsRandom) {
                et_password.setHint("请输入相册密码");
                et_account.setSelected(false);
                et_password.setSelected(false);
                et_password.setEnabled(true);
                et_account.setTextColor(Color.BLACK);
                mIsRandom = false;
            }

            //离开随机浏览，若相册名更改回随机相册名，输入框将变回不可编辑状态
            else if (s.toString().equals(mRandomName)) {
                et_account.setSelected(true);
                et_password.setSelected(true);
                et_password.setEnabled(false);
                et_password.setText("");
                et_password.setHint("");
                et_account.setTextColor(Color.GRAY);
                mIsRandom = true;
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}

    public static class AlbumResult extends PasswordDialog.Result {
        public String aid;
    }

    public static class Builder{

        protected AlbumPasswordDialog mDialog;

        public Builder(Context context) {
            mDialog = new AlbumPasswordDialog(context);
        }

        public PasswordDialog build() {
            return mDialog;
        }

        /**
         * 为dialog设置验证的url
         * @param url   验证账号密码是否正确的url。注意：该url必须包含name和password参数，参数值用占位符%s代替
         * @return  AlbumPasswordDialog
         */
        public Builder setVerifyUrl(String url) {
            mDialog.mVerifyUrl = url;
            return this;
        }

        //这种统一的方法为什么不在父类中定义呢，因为父类中不知道要返回对象的具体类型
        public Builder setOnResponseListener(OnResponseListener listener) {
            mDialog.mListener = listener;
            return this;
        }

        public Builder setTitle(String title) {
            mDialog.tv_title.setText(title);
            if (title == ENTER_ALBUM) {
                ViewStub stub = (ViewStub) mDialog.findViewById(R.id.stub);
                TextView tv_random=(TextView)stub.inflate();
                tv_random.setOnClickListener(mDialog);
            }
            return this;
        }

    }

}
