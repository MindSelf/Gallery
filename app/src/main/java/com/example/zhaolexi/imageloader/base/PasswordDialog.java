package com.example.zhaolexi.imageloader.base;

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

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.utils.MD5;

import org.json.JSONException;
import org.json.JSONObject;

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

public abstract class PasswordDialog<T extends PasswordDialog.Result> extends Dialog implements View.OnClickListener {

    protected static final String TAG = "PasswordDialog";
    protected static final int SUCCESS = 1;
    protected static final int FAIL = -1;

    protected Context mCtx;
    protected String mVerifyUrl;
    protected TextView tv_title, tv_account_name, tv_password_name,
            tv_negative, tv_positive;
    protected EditText et_account, et_password;
    protected OnResponseListener mListener;
    protected PasswordDialogHandler mHandler;
    protected OkHttpClient mClient;

    protected PasswordDialog(@NonNull Context context) {
        super(context);
        mCtx = context;
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
        mClient = new OkHttpClient();
        mHandler = new PasswordDialogHandler<T>(this);
    }

    protected abstract T newResult();

    protected boolean checkBeforeRequest() {
        return true;
    }

    protected abstract void onHandleData(JSONObject data, T result) throws JSONException;

    private void verify(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        mClient.newCall(request).enqueue(new Callback() {
            T result = newResult();

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e);
                result.msg = e.getMessage();
                Message.obtain(mHandler, FAIL, result).sendToTarget();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    result.msg = jsonObject.getString("msg");
                    int code = jsonObject.getInt("code");
                    if (code == SUCCESS) {
                        if (!jsonObject.isNull("data")) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            onHandleData(data, result);
                        }
                        Message.obtain(mHandler, SUCCESS, result).sendToTarget();
                    } else {
                        Message.obtain(mHandler, FAIL, result).sendToTarget();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_negative:
                dismiss();
                break;
            case R.id.tv_positive:
                if (checkBeforeRequest()) {
                    String name = et_account.getText().toString();
                    String password = et_password.getText().toString();
                    String url = String.format(mVerifyUrl, name, MD5.digest(password));
                    verify(url);
                }
                break;
        }
    }

    private static class PasswordDialogHandler<T extends Result> extends Handler {

        private SoftReference<PasswordDialog> mReference;

        public PasswordDialogHandler(PasswordDialog dialog) {
            mReference = new SoftReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mReference.get() != null) {
                PasswordDialog dialog = mReference.get();

                T result = (T) msg.obj;
                switch (msg.what) {
                    case SUCCESS:
                        dialog.mListener.onSuccess(result);
                        dialog.dismiss();
                        break;
                    case FAIL:
                        dialog.mListener.onFail(result.msg);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

    public interface OnResponseListener<T extends Result> {
        void onSuccess(T result);

        void onFail(String msg);
    }

    public static class Result {
        public String msg;
    }
}
