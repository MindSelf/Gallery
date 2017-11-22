package com.example.zhaolexi.imageloader.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.utils.MD5;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ZHAOLEXI on 2017/11/21.
 */

public class PasswordDialog extends Dialog implements View.OnClickListener {

    private static final int SUCCESS=1;
    private static final int FAIL=-1;

    private Context mCtx;
    private TextView mTitle,mNegative,mPositive;
    private EditText mAlbumName,mPassword;
    private String mUrl;
    private PasswordDialog.Builder.OnResponseListener mListener;
    private OkHttpClient mClient;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Result result=(Result) msg.obj;
            switch (msg.what) {
                case SUCCESS:
                    if (result.aid != null) {
                        mListener.onSuccess(result.aid);
                    }else{
                        mListener.onSuccess("");
                    }
                    dismiss();
                    break;
                default:
                    super.handleMessage(msg);
            }
            Toast.makeText(mCtx, result.msg, Toast.LENGTH_SHORT).show();
        }
    };

    private PasswordDialog(@NonNull Context context) {
        super(context);
        mCtx=context;
        initDialog();
    }

    private void initDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_password);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mAlbumName = (EditText) findViewById(R.id.et_album_name);
        mPassword = (EditText) findViewById(R.id.et_password);
        mNegative = (TextView) findViewById(R.id.tv_negative);
        mPositive = (TextView) findViewById(R.id.tv_positive);
        mNegative.setOnClickListener(this);
        mPositive.setOnClickListener(this);
        mClient=new OkHttpClient();
    }

    //不能包含空格
    private boolean isStringIllegal(String str) {
        return str.contains(" ");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_negative:
                dismiss();
                break;
            case R.id.tv_positive:
                String aname=mAlbumName.getText().toString();
                String apassword=mPassword.getText().toString();
                if (isStringIllegal(aname) || isStringIllegal(apassword)) {
                    Toast.makeText(mCtx, "相册名或密码不能包含空格", Toast.LENGTH_SHORT).show();
                    return;
                }
                String url = String.format(mUrl + "?aname=%s&apassword=%s", aname, MD5.digest(apassword));
                sendRequest(url);
                break;
            default:
                break;
        }
        mAlbumName.setText("");
        mPassword.setText("");
    }

    private void sendRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    Result result=new Result();
                    int code = jsonObject.getInt("code");
                    String msg = jsonObject.getString("msg");
                    result.msg=msg;
                    if (code == SUCCESS) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        String aid=null;
                        if (data != null) {
                            //如果是删除相册，data为null
                            aid = data.getString("aid");
                        }
                        result.aid=aid;
                        Message.obtain(mHandler, SUCCESS, result).sendToTarget();
                    }else {
                        Message.obtain(mHandler, FAIL, result).sendToTarget();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class Builder{
        private PasswordDialog dialog;

        public Builder(Context context) {
            dialog = new PasswordDialog(context);
        }

        public Builder setTitle(String title) {
            dialog.mTitle.setText(title);
            return this;
        }

        public Builder setUrl(String url) {
            dialog.mUrl = url;
            return this;
        }

        public Builder setOnResponseListener(OnResponseListener listener) {
            dialog.mListener=listener;
            return this;
        }

        public PasswordDialog build() {
            return dialog;
        }

        public interface OnResponseListener{
            void onSuccess(String aid);
        }
    }

    private class Result{
        String aid;
        String msg;
    }

}
