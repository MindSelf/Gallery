package com.example.zhaolexi.imageloader.home.gallery;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * Created by ZHAOLEXI on 2018/1/24.
 */

public class FabBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    private static long DURATION = 150; //milliseconds
    private boolean mIsAnimating, mIsVisible;
    private ScaleAnimation mOpenAnimation, mCloseAnimation;

    public FabBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIsVisible = true;
        mOpenAnimation = new ScaleAnimation(0, 1f, 0, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mOpenAnimation.setDuration(DURATION);
        mOpenAnimation.setFillAfter(true);
        mOpenAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsAnimating = false;
                mIsVisible = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mCloseAnimation = new ScaleAnimation(1f, 0, 1f, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mCloseAnimation.setDuration(DURATION);
        mCloseAnimation.setFillAfter(true);
        mCloseAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsAnimating = false;
                mIsVisible = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        //响应垂直方向上的滚动
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dx, int dy, int[] consumed) {
        if (mIsVisible && dy > 0 && !mIsAnimating) {
            //往下滑,关闭
            child.startAnimation(mCloseAnimation);
        } else if (!mIsVisible && dy < 0 && !mIsAnimating) {
            //往上滑，展开
            child.startAnimation(mOpenAnimation);
        }
    }

    public void startOpening(FloatingActionButton view) {
        if(view.getVisibility()!=View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(mOpenAnimation);
        }
    }

    public void startClosing(FloatingActionButton view) {
        if(view.getVisibility()==View.VISIBLE) {
            view.startAnimation(mCloseAnimation);
            //由于动画使用了animation.setFillAfter(true），这会使mCurrentAnimation在动画结束时不置空
            //当ViewGroup在绘制子View时，如果子View的mCurrentAnimation不为null，就算不为visible也照样会进行绘制
            view.clearAnimation();
            view.setVisibility(View.GONE);
        }
    }
}
