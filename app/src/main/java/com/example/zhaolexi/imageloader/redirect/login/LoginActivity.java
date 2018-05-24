package com.example.zhaolexi.imageloader.redirect.login;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
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
    private boolean mIsPasswordVisible;
    private boolean mHasPhotoClearShown;
    private boolean mHasNameClearShown;


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
                if (s.length() > 0 && !mHasPhotoClearShown) {
                    showPhotoClear();
                } else if (s.length() <= 0 && mHasPhotoClearShown) {
                    dismissPhotoClear();
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
                if (s.length() > 0 && !mHasNameClearShown) {
                    showNameClear();
                } else if (s.length() <= 0 && mHasNameClearShown) {
                    dismissNameClear();
                }
            }
        });
        mName.setOnTouchListener(this);
        mName.setOnFocusChangeListener(this);

        mPassword = (EditText) findViewById(R.id.et_password);
        mPassword.setOnTouchListener(this);
        mPassword.setOnFocusChangeListener(this);

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
        setPasswordInvisible();
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
                if (!mHasPhotoClearShown && hasFocus && mMobile.getText().length() > 0) {
                    showPhotoClear();
                } else if (mHasPhotoClearShown && !hasFocus && mMobile.getText().length() <= 0) {
                    dismissPhotoClear();
                }
                break;
            case R.id.et_name:
                if (!mHasNameClearShown && hasFocus && mName.getText().length() > 0) {
                    showNameClear();
                } else if (mHasNameClearShown && !hasFocus && mName.getText().length() <= 0) {
                    dismissNameClear();
                }
                break;
        }
    }

    private void dismissNameClear() {
        mName.setCompoundDrawables(null, null, null, null);
        mHasNameClearShown = false;
    }

    private void showNameClear() {
        Drawable clear = getResources().getDrawable(R.mipmap.ic_close_white);
        clear.setBounds(0, 0, clear.getIntrinsicWidth(), clear.getIntrinsicHeight());
        mName.setCompoundDrawables(null, null, clear, null);
        mHasNameClearShown = true;
    }

    private void dismissPhotoClear() {
        mMobile.setCompoundDrawables(null, null, null, null);
        mHasPhotoClearShown = false;
    }

    private void showPhotoClear() {
        //这一步必须要做,否则不会显示.
        Drawable clear = getResources().getDrawable(R.mipmap.ic_close_white);
        clear.setBounds(0, 0, clear.getIntrinsicWidth(), clear.getIntrinsicHeight());
        mMobile.setCompoundDrawables(null, null, clear, null);
        mHasPhotoClearShown = true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHasTouchDrawable = isTouchWithinDrawable(v, event);
                break;
            case MotionEvent.ACTION_UP:
                if (mHasTouchDrawable && isTouchWithinDrawable(v, event)) {
                    switch (v.getId()) {
                        case R.id.et_mobile:
                        case R.id.et_name:
                            ((EditText) v).setText("");
                            break;
                        case R.id.et_password:
                            if (mIsPasswordVisible) {
                                setPasswordInvisible();
                            } else {
                                setPasswordVisible();
                            }
                            break;
                    }
                }
                break;
        }
        return false;
    }

    private void setPasswordVisible() {
        mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        Drawable invisible = getResources().getDrawable(R.mipmap.ic_visibility);
        invisible.setBounds(0, 0, invisible.getIntrinsicWidth(), invisible.getIntrinsicHeight());
        mPassword.setCompoundDrawables(null, null, invisible, null);
        mIsPasswordVisible = true;
    }

    private void setPasswordInvisible() {
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        Drawable visible = getResources().getDrawable(R.mipmap.ic_visibility_off);
        visible.setBounds(0, 0, visible.getIntrinsicWidth(), visible.getIntrinsicHeight());
        mPassword.setCompoundDrawables(null, null, visible, null);
        mIsPasswordVisible = false;
    }

    private boolean isTouchWithinDrawable(View v, MotionEvent event) {
        EditText editText = (EditText) v;
        Drawable drawable = editText.getCompoundDrawables()[2];
        if (drawable != null && event.getX() >= v.getWidth() - v.getPaddingEnd() - drawable.getIntrinsicWidth()) {
            return true;
        }
        return false;
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
