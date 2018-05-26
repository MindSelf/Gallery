package com.example.zhaolexi.imageloader.redirect.access;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.example.zhaolexi.imageloader.common.net.DefaultCookieJar;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.utils.EncryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.ref.SoftReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ZHAOLEXI on 2018/1/10.
 */

public class PasswordDialog<T> extends Dialog implements View.OnClickListener {

    protected static final String TAG = "PasswordDialog";
    static final int SUCCESS = 1;
    protected static final int FAIL = -1;

    protected String mVerifyUrl;
    protected TextView tv_title, tv_account_name, tv_password_name,
            tv_negative, tv_positive;
    protected EditText et_account, et_password;
    protected OnRequestFinishListener<T> mCallback;
    protected PasswordDialogHandler<T> mHandler;
    private OkHttpClient mClient;

    PasswordDialog(@NonNull Context context) {
        super(context);
        initDialog();
    }

    private void initDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_password);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_account_name = (TextView) findViewById(R.id.tv_account_name);
        tv_password_name = (TextView) findViewById(R.id.tv_password_name);
        et_account = (EditText) findViewById(R.id.et_account_name);
        et_password = (EditText) findViewById(R.id.et_password);
        tv_negative = (TextView) findViewById(R.id.tv_negative);
        tv_positive = (TextView) findViewById(R.id.tv_positive);

        tv_negative.setOnClickListener(this);
        tv_positive.setOnClickListener(this);

        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
        mHandler = new PasswordDialogHandler<>(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_negative:
                dismiss();
                break;
            case R.id.tv_positive:
                if (checkBeforeRequest()) {
                    String account = et_account.getText().toString();
                    String password = et_password.getText().toString();
                    String url = String.format(mVerifyUrl, account, EncryptUtil.digest(password));
                    verify(url);
                }
                break;
        }
    }

    protected boolean checkBeforeRequest() {
        return true;
    }

    protected void onHandleData(T data) {
    }

    protected Callback newCallback(String url) {
        return new DefaultCallback();
    }

    protected void verify(final String url) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        mClient.newCall(request).enqueue(newCallback(url));
    }

    private static class PasswordDialogHandler<T> extends Handler {

        private SoftReference<PasswordDialog<T>> mReference;

        PasswordDialogHandler(PasswordDialog<T> dialog) {
            mReference = new SoftReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mReference.get() != null) {
                PasswordDialog dialog = mReference.get();

                switch (msg.what) {
                    case SUCCESS:
                        T data = (T) msg.obj;
                        dialog.onHandleData(data);
                        dialog.mCallback.onSuccess(data);
                        dialog.dismiss();
                        break;
                    case FAIL:
                        String hint = (String) msg.obj;
                        Toast.makeText(dialog.getContext(), hint, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

    protected class DefaultCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.e(TAG, "onFailure: " + e);
            Message.obtain(mHandler, FAIL, e.getMessage()).sendToTarget();
        }

        @Override
        public void onResponse(Call call, Response response) {
            Log.d(TAG, "onResponse: " + response);
            try {
                Result result = new Gson().fromJson(response.body().string(), Result.class);
                if (result != null) {
                    if (result.isSuccess()) {
                        Message.obtain(mHandler, SUCCESS, result.getData()).sendToTarget();
                    } else {
                        Message.obtain(mHandler, FAIL, result.getMsg()).sendToTarget();
                    }
                }
            } catch (JsonSyntaxException | IOException e) {
                Message.obtain(mHandler, FAIL, getContext().getString(R.string.json_error)).sendToTarget();
            }
        }
    }

    public static abstract class Builder<V extends PasswordDialog, T> {

        Context mCtx;
        String mVerifyUrl;
        String mTitle;
        String mAccountName, mAccountHint, mAccountDef;
        String mPasswordName, mPasswordHint, mPasswordDef;
        OnRequestFinishListener<T> mCallback;

        private static final class DefaultCallback<T> implements OnRequestFinishListener<T> {

            @Override
            public void onSuccess(T data) {

            }

            @Override
            public void onFail(String reason, Result result) {

            }
        }

        public Builder(Context context) {
            mCtx = context;
            mTitle = "PasswordDialog";
            mAccountName = "account";
            mAccountHint = "please input your account";
            mPasswordName = "password";
            mPasswordHint = "please input your password";
            mCallback = new DefaultCallback<T>();
        }

        public V build() {
            V dialog = newInstance();
            dialog.mVerifyUrl = mVerifyUrl;
            dialog.mCallback = mCallback;
            dialog.tv_title.setText(mTitle);
            dialog.tv_account_name.setText(mAccountName);
            dialog.et_account.setHint(mAccountHint);
            dialog.et_account.setText(mAccountDef);
            dialog.tv_password_name.setText(mPasswordName);
            dialog.et_password.setHint(mPasswordHint);
            dialog.et_password.setText(mPasswordDef);
            return dialog;
        }

        protected abstract V newInstance();


        /**
         * 为dialog设置验证的url
         *
         * @param url 验证账号密码是否正确的url。注意：该url必须包含name和password参数，参数值用占位符%s代替
         * @return AlbumPasswordDialog
         */
        public Builder setVerifyUrl(String url) {
            mVerifyUrl = url;
            return this;
        }

        public Builder setCallback(OnRequestFinishListener<T> listener) {
            mCallback = listener;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setAccount(String name, String hint) {
            mAccountName = name;
            mAccountHint = hint;
            return this;
        }

        public Builder setAccountDef(String def) {
            mAccountDef = def;
            return this;
        }

        public Builder setPassword(String name, String hint) {
            mPasswordName = name;
            mPasswordHint = hint;
            return this;
        }

        public Builder setPasswordDef(String def) {
            mPasswordDef = def;
            return this;
        }

    }

}
