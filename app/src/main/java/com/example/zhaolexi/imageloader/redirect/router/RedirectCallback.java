package com.example.zhaolexi.imageloader.redirect.router;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


public abstract class RedirectCallback extends Binder {

    private static final String TAG = "RedirectCallback";
    public static final String descriptor = "android.os.IBinder";
    public static final int FAIL = 0;
    public static final int SUCCESS = 1;
    public static final int CALLBACK = 2;

    @Override
    protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        if (code == CALLBACK) {
            try {
                data.enforceInterface(descriptor);
                int isSuccess = data.readInt();
                onCallback(isSuccess == SUCCESS);
                reply.writeNoException();
                return true;
            } catch (Exception e) {
                Log.w(TAG, "onTransact: ", e);
            }
        }
        return super.onTransact(code, data, reply, flags);
    }

    protected abstract void onCallback(boolean success);
}
