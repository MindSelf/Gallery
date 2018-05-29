package com.example.zhaolexi.imageloader.me.info;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.common.ui.PasswordDialog;
import com.example.zhaolexi.imageloader.common.utils.EncryptUtils;
import com.example.zhaolexi.imageloader.common.utils.SharePreferencesUtils;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ModifyPasswordDialog extends PasswordDialog {

    private EditText mOldPassword, mNewPassword;


    @SuppressLint("HandlerLeak")
    ModifyPasswordDialog(@NonNull Context context) {
        super(context);
        mOldPassword = et_account;
        mNewPassword = et_password;
        mOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
        if (mOldPassword.getText().toString().equals(mNewPassword.getText().toString())) {
            Toast.makeText(getContext(), "新密码和旧密码一致", Toast.LENGTH_SHORT).show();
            return false;
        }
        String newPassword = mNewPassword.getText().toString();
        CheckResult result = checkField(newPassword, 6, 15);
        if (result == CheckResult.EMPTY) {
            Toast.makeText(getContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (result == CheckResult.OUT_OF_BOUND) {
            Toast.makeText(getContext(), "密码长度必须在6~15位之间", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return super.checkBeforeRequest();
        }
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

    private CheckResult checkField(String field, int min, int max) {
        if (TextUtils.isEmpty(field)) {
            return CheckResult.EMPTY;
        }
        if (field.length() < min || field.length() > max) {
            return CheckResult.OUT_OF_BOUND;
        }
        return CheckResult.OK;
    }

    enum CheckResult {
        EMPTY, OUT_OF_BOUND, OK,
    }

    public static class Builder extends PasswordDialog.Builder<ModifyPasswordDialog, Album> {

        public Builder(Context context) {
            super(context);
            setVerifyUrl(Uri.MODIFY_PASSWORD + "&uid=" + SharePreferencesUtils.getString(SharePreferencesUtils.MOBILE, ""));
            setAccount("旧密码", "请输入旧密码");
            setPassword("新密码", "请输入新密码");
            setTitle("修改密码");
        }

        @Override
        protected ModifyPasswordDialog newInstance() {
            return new ModifyPasswordDialog(mCtx);
        }
    }
}
