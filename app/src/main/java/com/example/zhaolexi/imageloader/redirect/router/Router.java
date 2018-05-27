package com.example.zhaolexi.imageloader.redirect.router;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.home.gallery.GalleryActivity;
import com.example.zhaolexi.imageloader.home.manager.Album;

public class Router {

    private static final String TAG = "Router";
    private Context mContext;
    private RedirectCallback mCallback;
    private Album mOrigin;
    public static volatile boolean isRouting;

    private Router(Activity activity) {
        mContext = activity == null ? BaseApplication.getContext() : activity;
    }

    /**
     * 跳转到进入相册对话框或者登陆界面
     *
     * @param result
     * @return is server error
     */
    public boolean route(Result result) {
        if (isTokenError(result)) {
            startRouterActivity(mCallback);
            return false;
        }

        if (isPermissionDenied(result)) {
            Activity activity;
            if (mContext instanceof Activity) {
                activity = (Activity) mContext;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        accessAlbum(mOrigin);
                    }
                });
            }
            return false;
        } else {
            Activity activity;
            if (mContext instanceof Activity) {
                activity = (Activity) mContext;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "服务器异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            Log.d(TAG, "result code = : " + result.getCode() + "，msg =" + result.getMsg());
            return true;
        }
    }

    private boolean isTokenError(Result result) {
        return result.getCode() == Result.TOKEN_ERROR;
    }

    private boolean isPermissionDenied(Result result) {
        return result.getCode() == Result.PERMISSION_DENIED;
    }

    private void accessAlbum(final Album origin) {
        if (!isRouting) {
            isRouting = true;
            Context context = mContext == null ? BaseApplication.getContext() : mContext;
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.reaccess_album))
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(mContext, GalleryActivity.class);
                            intent.putExtra(GalleryActivity.ORIGIN_ALBUM, origin);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            if (mContext instanceof Application) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            }
                            mContext.startActivity(intent);
                            if (mContext instanceof Activity) {
                                ((Activity) mContext).overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                            }
                            isRouting = false;
                        }
                    }).show();
        }
    }

    private void startRouterActivity(RedirectCallback callback) {
        if (!isRouting) {
            isRouting = true;
            Intent intent = new Intent(mContext, RouterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putBinder(RouterActivity.EXTRA_CALLBACK, callback);
            intent.putExtra(RouterActivity.EXTRA_BUNDLE, bundle);
            if (mContext instanceof Application) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            mContext.startActivity(intent);
        }
    }

    public static class Builder {
        Activity context;
        Router router;
        RedirectCallback loginCallback;
        Album originAlbum;

        public Builder(Activity activity) {
            context = activity;
        }

        /*
        only LOAD_IMG、GET_RANDOM and ADD_ALBUM will redirect to login
         */
        public Builder setLoginCallback(RedirectCallback loginCallback) {
            this.loginCallback = loginCallback;
            return this;
        }

        /*
        only IMAGE operation will reaccess album
         */
        public Builder setOriginAlbum(Album originAlbum) {
            this.originAlbum = originAlbum;
            return this;
        }

        public Router build() {
            router = new Router(context);
            router.mCallback = loginCallback;
            router.mOrigin = originAlbum;
            return router;
        }
    }
}
