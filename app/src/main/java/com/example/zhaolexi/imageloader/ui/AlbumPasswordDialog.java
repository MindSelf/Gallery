package com.example.zhaolexi.imageloader.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.base.PasswordDialog;
import com.example.zhaolexi.imageloader.bean.Album;
import com.example.zhaolexi.imageloader.utils.Uri;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ZHAOLEXI on 2018/2/7.
 */

public class AlbumPasswordDialog extends PasswordDialog<AlbumPasswordDialog.AlbumResult> {

    public static final String CREATE_ALBUM = "创建相册";
    public static final String ADD_ALBUM = "添加相册";

    protected AlbumPasswordDialog(@NonNull Context context) {
        super(context);
        tv_account_name.setText("相册名");
        tv_password_name.setText("相册密码");
        et_account.setHint("请输入相册名称");
        et_password.setHint("请输入相册密码");
    }

    @Override
    protected AlbumResult newResult() {
        return new AlbumResult();
    }

    @Override
    protected void onHandleData(JSONObject data, AlbumResult result) throws JSONException {
        //获取相册信息
        Gson gson = new Gson();
        Album album = gson.fromJson(data.toString(), Album.class);
        //构造数据
        album.setUrl(Uri.Load_Img + "&album.aid=" + album.getAid() + "&currPage=");
        album.setTitle(et_account.getText().toString());
        album.setAccessible(true);
        result.album = album;
    }

    @Override
    protected boolean checkBeforeRequest() {
        String name = et_account.getText().toString();
        String password = et_password.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "相册名或密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isStringIllegal(name) || isStringIllegal(password)) {
            Toast.makeText(getContext(), "相册名或密码不能包含空格", Toast.LENGTH_SHORT).show();
            return false;
        }

        return super.checkBeforeRequest();
    }

    private boolean isStringIllegal(String str) {
        //不能包含空格
        return str.contains(" ");
    }

    public static class AlbumResult extends PasswordDialog.Result {
        public Album album;
    }

    public static class Builder {

        protected AlbumPasswordDialog mDialog;

        public Builder(Context context) {
            mDialog = new AlbumPasswordDialog(context);
        }

        public PasswordDialog build() {
            return mDialog;
        }

        /**
         * 为dialog设置验证的url
         *
         * @param url 验证账号密码是否正确的url。注意：该url必须包含name和password参数，参数值用占位符%s代替
         * @return AlbumPasswordDialog
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
            return this;
        }

    }
}
