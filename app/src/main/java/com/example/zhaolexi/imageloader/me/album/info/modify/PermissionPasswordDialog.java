package com.example.zhaolexi.imageloader.me.album.info.modify;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.common.ui.PasswordDialog;
import com.example.zhaolexi.imageloader.common.utils.EncryptUtils;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PermissionPasswordDialog extends PasswordDialog {

    private EditText mReadPassword, mModPassword;


    @SuppressLint("HandlerLeak")
    PermissionPasswordDialog(@NonNull Context context) {
        super(context);
        mReadPassword = et_account;
        mModPassword = et_password;
        mReadPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                switch (msg.what) {
                    case SUCCESS:
                        mCallback.onSuccess(null);
                        dismiss();
                        break;
                    case FAIL:
                        mCallback.onFail((String) msg.obj, null);
                        break;
                }
            }
        };
    }

    @Override
    protected String onHandleAccount(String account) {
        return EncryptUtils.digest(account);
    }

    @Override
    protected boolean checkBeforeRequest() {
        if (mReadPassword.getText().length() == 0 && mModPassword.getText().length() == 0) {
            Toast.makeText(getContext(), "密码不能同时为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mReadPassword.getText().length() != 0 && !checkField(mReadPassword.getText().toString(), 6, 15)) {
            mReadPassword.requestFocus();
            Toast.makeText(getContext(), "密码长度必须在6~15位之间", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mModPassword.getText().length() != 0 && !checkField(mModPassword.getText().toString(), 6, 15)) {
            mModPassword.requestFocus();
            Toast.makeText(getContext(), "密码长度必须在6~15位之间", Toast.LENGTH_SHORT).show();
            return false;
        }
        return super.checkBeforeRequest();
    }

    @Override
    protected Callback newCallback(String url) {
        return new DefaultCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message.obtain(mHandler, FAIL, "服务器异常").sendToTarget();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Result result = new Gson().fromJson(response.body().string(), Result.class);
                    if (result.isSuccess()) {
                        Message.obtain(mHandler, SUCCESS, "修改成功").sendToTarget();
                    } else {
                        Message.obtain(mHandler, FAIL, result.getMsg()).sendToTarget();
                    }
                } catch (IOException | JsonSyntaxException e) {
                    Message.obtain(mHandler, FAIL, getContext().getString(R.string.json_error)).sendToTarget();
                }
            }
        };
    }

    private boolean checkField(String field, int min, int max) {
        if (field.length() < min || field.length() > max) {
            return false;
        }
        return true;
    }

    public static class Builder extends PasswordDialog.Builder<PermissionPasswordDialog, Album> {

        public Builder(Context context, Album album) {
            super(context);
            setVerifyUrl(Uri.MODIFY_ALBUM + "?aid=" + album.getAid() + "&who=" + album.getWho() + "&title=" + album.getTitle() + "&adesc="
                    + album.getAdesc() + "&readPassword=%s&modPassword=&s");
            setAccount(context.getString(R.string.read_password), context.getString(R.string.read_password_hint));
            setPassword(context.getString(R.string.mod_password), context.getString(R.string.mod_password_hint));
            setTitle("设置分享码");
        }

        @Override
        protected PermissionPasswordDialog newInstance() {
            return new PermissionPasswordDialog(mCtx);
        }
    }
}
