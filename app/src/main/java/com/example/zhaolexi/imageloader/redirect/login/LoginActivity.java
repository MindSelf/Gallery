package com.example.zhaolexi.imageloader.redirect.login;

import android.app.Activity;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseActivity;
import com.example.zhaolexi.imageloader.common.utils.CompoundDrawableUtils;
import com.example.zhaolexi.imageloader.redirect.router.Router;

public class LoginActivity extends BaseActivity<LoginPresenter> implements LoginViewInterface, View.OnClickListener, View.OnTouchListener, View.OnFocusChangeListener {

    private TextView mSignUp;
    private EditText mMobile, mName, mPassword;
    private Button mLogin;
    private RelativeLayout mNameLayout;

    private QueryMobileCallback mCallback;

    private boolean mIsInSignUpMode;

    private boolean mHasQuerySuccess;
    private boolean mHasMobileRegistered;

    private boolean mHasTouchDrawable;
    private boolean mHasPhoneClearShown;
    private boolean mHasNameClearShown;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Router.isRouting = false;
    }

    @Override
    protected void initData() {
        mCallback = new QueryMobileCallback() {
            @Override
            public void hasMobileRegistered(boolean hasRegisted) {
                mHasMobileRegistered = hasRegisted;
            }

            @Override
            public void hasQuerySuccess(boolean hasSuccess) {
                mHasQuerySuccess = hasSuccess;
            }
        };
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_login);

        mSignUp = (TextView) findViewById(R.id.tv_sign_up);
        initSignUp();

        mNameLayout = (RelativeLayout) findViewById(R.id.rl_name);
        mNameLayout.setVisibility(View.GONE);

        mMobile = (EditText) findViewById(R.id.et_mobile);
        mMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s) && !mHasPhoneClearShown) {
                    showPhoneClear();
                } else if (TextUtils.isEmpty(s) && mHasPhoneClearShown) {
                    dismissPhoneClear();
                }
                if (s.length() == 11) {
                    mPresenter.hasMobileRegisted(s.toString(), mCallback);
                }
            }
        });
        mMobile.setOnTouchListener(this);
        mMobile.setOnFocusChangeListener(this);

        mName = (EditText) findViewById(R.id.et_name);
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s) && !mHasNameClearShown) {
                    showNameClear();
                } else if (TextUtils.isEmpty(s) && mHasNameClearShown) {
                    dismissNameClear();
                }
            }
        });
        mName.setOnTouchListener(this);
        mName.setOnFocusChangeListener(this);

        mPassword = (EditText) findViewById(R.id.et_password);
        mPassword.setOnTouchListener(this);

        mLogin = (Button) findViewById(R.id.btn_login_in);
        mLogin.setOnClickListener(this);
    }


    private void initSignUp() {
        SpannableString ss = new SpannableString(getString(R.string.sign_up));
        ss.setSpan(new ForegroundColorSpan(Color.WHITE), 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                mNameLayout.setVisibility(View.VISIBLE);
                mLogin.setText(getString(R.string.register));
                clearData();
                initSignIn();
            }
        }, 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSignUp.setText(ss);
        mSignUp.setMovementMethod(LinkMovementMethod.getInstance());
        mIsInSignUpMode = false;
    }

    private void initSignIn() {
        SpannableString ss = new SpannableString(getString(R.string.sign_in));
        ss.setSpan(new ForegroundColorSpan(Color.WHITE), 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                mNameLayout.setVisibility(View.GONE);
                mLogin.setText(getString(R.string.login_in));
                clearData();
                initSignUp();
            }
        }, 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSignUp.setText(ss);
        mSignUp.setMovementMethod(LinkMovementMethod.getInstance());
        mIsInSignUpMode = true;
    }

    private void clearData() {
        mMobile.setText("");
        mName.setText("");
        mPassword.setText("");
        CompoundDrawableUtils.setPasswordInvisible(mPassword, R.mipmap.ic_visibility_off);
        mHasQuerySuccess = false;
        mHasMobileRegistered = false;
        mHasTouchDrawable = false;
    }

    @Override
    protected LoginPresenter createPresenter() {
        return new LoginPresenter();
    }

    @Override
    public void showHint(String hint) {
        Toast.makeText(this, hint, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login_in:

                if (!mPresenter.checkMobile(mMobile.getText().toString())) {
                    return;
                } else if (mIsInSignUpMode) {
                    if (!mHasQuerySuccess) {
                        mPresenter.hasMobileRegisted(mMobile.getText().toString(), mCallback);
                        showHint("服务器异常，请重试");
                        return;
                    }

                    if (mHasMobileRegistered) {
                        showHint("该账号已被注册");
                        return;
                    }
                }

                if (mIsInSignUpMode && !mPresenter.checkName(mName.getText().toString())) {
                    return;
                }

                if (!mPresenter.checkPassword(!mIsInSignUpMode, mPassword.getText().toString())) {
                    return;
                }

                //字段均符合规则才能登录/注册
                if (mIsInSignUpMode) {
                    mPresenter.register(mMobile.getText().toString(), mName.getText().toString(), mPassword.getText().toString());
                } else {
                    mPresenter.login(mMobile.getText().toString(), mPassword.getText().toString());
                }
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_mobile:
                if (!mHasPhoneClearShown && hasFocus && !TextUtils.isEmpty(mMobile.getText())) {
                    showPhoneClear();
                } else if (mHasPhoneClearShown && !hasFocus) {
                    dismissPhoneClear();
                }
                break;
            case R.id.et_name:
                if (!mHasNameClearShown && hasFocus && !TextUtils.isEmpty(mName.getText())) {
                    showNameClear();
                } else if (mHasNameClearShown && !hasFocus) {
                    dismissNameClear();
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHasTouchDrawable = CompoundDrawableUtils.isTouchWithinDrawable(v, event);
                break;
            case MotionEvent.ACTION_UP:
                if (mHasTouchDrawable && CompoundDrawableUtils.isTouchWithinDrawable(v, event)) {
                    switch (v.getId()) {
                        case R.id.et_mobile:
                        case R.id.et_name:
                            ((EditText) v).setText("");
                            break;
                        case R.id.et_password:
                            if (CompoundDrawableUtils.isPasswordVisible((EditText) v)) {
                                CompoundDrawableUtils.setPasswordInvisible((EditText) v, R.mipmap.ic_visibility_off);
                            } else {
                                CompoundDrawableUtils.setPasswordVisible((EditText) v, R.mipmap.ic_visibility);
                            }
                            break;
                    }
                }
                break;
        }
        return false;
    }

    private void showPhoneClear() {
        CompoundDrawableUtils.showEditDrawable(mMobile, R.mipmap.ic_close_white);
        mHasPhoneClearShown = true;
    }

    private void dismissPhoneClear() {
        CompoundDrawableUtils.dismissEditDrawable(mMobile);
        mHasPhoneClearShown = false;
    }

    private void showNameClear() {
        CompoundDrawableUtils.showEditDrawable(mName, R.mipmap.ic_close_white);
        mHasNameClearShown = true;
    }

    private void dismissNameClear() {
        CompoundDrawableUtils.dismissEditDrawable(mName);
        mHasNameClearShown = false;
    }

    @Override
    public Activity getContactActivity() {
        return this;
    }

    public interface QueryMobileCallback {
        void hasMobileRegistered(boolean hasRegistered);

        void hasQuerySuccess(boolean hasSuccess);
    }
}
