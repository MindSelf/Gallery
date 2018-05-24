package com.example.zhaolexi.imageloader.redirect.router;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zhaolexi.imageloader.redirect.login.LoginActivity;

public class RouterActivity extends Activity {

    private static final String TAG = "RouterActivity";
    private static final int REQUEST_CODE = 1;
    public static final String EXTRA_BUNDLE = "bundle";
    public static final String EXTRA_CALLBACK = "callback";
    private RedirectCallback mCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getBundleExtra(EXTRA_BUNDLE);
        if (bundle != null) {
            mCallback = (RedirectCallback) bundle.getBinder(EXTRA_CALLBACK);
        }
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE && mCallback != null && mCallback.isBinderAlive()) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            //否则会出现 java.lang.SecurityException: Binder invocation to an incorrect interface
            data.writeInterfaceToken(RedirectCallback.descriptor);
            if (resultCode == RESULT_OK) {
                data.writeInt(RedirectCallback.SUCCESS);
            } else {
                data.writeInt(RedirectCallback.FAIL);
            }
            try {
                mCallback.transact(RedirectCallback.CALLBACK, data, reply, 0);
            } catch (RemoteException e) {
                Log.w(TAG, "onActivityResult: ", e);
            }
            reply.readException();
            data.recycle();
            reply.recycle();
        }
        super.onActivityResult(requestCode, resultCode, intent);
        finish();
    }
}
