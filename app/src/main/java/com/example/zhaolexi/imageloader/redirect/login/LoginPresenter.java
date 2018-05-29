package com.example.zhaolexi.imageloader.redirect.login;

import android.app.Activity;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.common.base.BasePresenter;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.utils.EncryptUtils;
import com.example.zhaolexi.imageloader.redirect.router.Result;

public class LoginPresenter extends BasePresenter<LoginViewInterface, LoginModel> {

    private static final String TEL_REGEX = "^((13[0-9])|(14[5,7,9])|(15[^4])|(18[0-9])|(17[0,1,3,5,6,7,8]))\\d{8}$";

    @Override
    protected LoginModel newModel() {
        return new loginModelImpl();
    }

    @Override
    protected void onMessageSuccess(Message msg) {
        if (isViewAttached()) {
            String hint = (String) msg.obj;
            Activity activity = (Activity) getView();
            Toast.makeText(activity, hint, Toast.LENGTH_SHORT).show();

            activity.setResult(Activity.RESULT_OK);
            activity.finish();
        }
    }

    @Override
    protected void onMessageFail(Message msg) {
        if (isViewAttached()) {
            String hint = (String) msg.obj;
            Activity activity = (Activity) getView();
            Toast.makeText(activity, hint, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkMobile(String mobile) {
        if (isViewAttached()) {
            if (TextUtils.isEmpty(mobile)) {
                getView().showHint("手机号不能为空");
                return false;
            }
            if (!isMobile(mobile)) {
                getView().showHint("请输入正确的手机号");
                return false;
            }
            return true;
        }
        return false;
    }

    public void hasMobileRegisted(String mobile, final LoginActivity.QueryMobileCallback callback) {
        if (!isMobile(mobile)) return;

        mModel.checkMobile(mobile, new CheckMobileCallback() {
            @Override
            public void hasRegist(boolean hasRegisted, String message) {
                if (isViewAttached()) {
                    callback.hasMobileRegistered(hasRegisted);
                    callback.hasQuerySuccess(true);
                }
            }

            @Override
            public void onRequestFail(String reason) {
                if (isViewAttached()) {
                    callback.hasQuerySuccess(false);
                }
            }
        });
    }

    private boolean isMobile(String mobile) {
        return mobile.matches(TEL_REGEX);
    }

    public boolean checkName(String name) {
        CheckResult result = checkField(name, 1, 6);
        if (isViewAttached()) {
            switch (result) {
                case EMPTY:
                    getView().showHint("用户名不能为空");
                    return false;
                case OUT_OF_BOUND:
                    getView().showHint("用户名长度必须在1~6位之间");
                    return false;
                default:
                    return true;
            }
        }
        return false;
    }

    public boolean checkPassword(boolean isLogin, String password) {
        CheckResult result = checkField(password, 6, 15);
        if (isViewAttached()) {
            switch (result) {
                case EMPTY:
                    getView().showHint("密码不能为空");
                    return false;
                case OUT_OF_BOUND:
                    if (!isLogin) {
                        getView().showHint("密码长度必须在6~15位之间");
                        return false;
                    } else {
                        return true;
                    }
                default:
                    return true;
            }
        }
        return false;
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

    public void login(String mobile, String password) {
        mModel.login(mobile, EncryptUtils.digest(password), new OnRequestFinishListener<User>() {
            @Override
            public void onSuccess(User data) {
                Message.obtain(mHandler, MSG_SUCCESS, "登录成功！").sendToTarget();
            }

            @Override
            public void onFail(String reason, Result result) {
                Message.obtain(mHandler, MSG_FAIL, reason).sendToTarget();
            }
        });
    }

    public void register(String mobile, String name, String password) {
        mModel.register(mobile, name, EncryptUtils.digest(password), new OnRequestFinishListener<User>() {
            @Override
            public void onSuccess(User data) {
                Message.obtain(mHandler, MSG_SUCCESS, "注册成功！").sendToTarget();
            }

            @Override
            public void onFail(String reason, Result result) {
                Message.obtain(mHandler, MSG_FAIL, reason).sendToTarget();
            }
        });
    }

    enum CheckResult {
        EMPTY, OUT_OF_BOUND, OK,
    }

    public interface CheckMobileCallback {
        void hasRegist(boolean hasRegisted, String message);

        void onRequestFail(String reason);
    }

}
