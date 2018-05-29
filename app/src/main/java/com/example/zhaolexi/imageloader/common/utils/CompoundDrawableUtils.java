package com.example.zhaolexi.imageloader.common.utils;

import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.example.zhaolexi.imageloader.common.base.BaseApplication;

public class CompoundDrawableUtils {

    public static boolean isTouchWithinDrawable(View v, MotionEvent event) {
        EditText editText = (EditText) v;
        Drawable drawable = editText.getCompoundDrawables()[2];
        if (drawable != null && event.getX() >= v.getWidth() - v.getPaddingEnd() - drawable.getIntrinsicWidth()) {
            return true;
        }
        return false;
    }



    public static void showEditDrawable(EditText editText, int res) {
        Drawable drawable = BaseApplication.getContext().getResources().getDrawable(res);
        //这一步必须要做,否则不会显示
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        editText.setCompoundDrawables(null, null, drawable, null);
    }

    public static void showEditDrawable(EditText editText, int res, int reqWidth, int reqHeight) {
        Drawable drawable = BaseApplication.getContext().getResources().getDrawable(res);
        drawable.setBounds(0, 0, reqWidth, reqHeight);
        editText.setCompoundDrawables(null, null, drawable, null);
    }

    public static void dismissEditDrawable(EditText editText) {
        editText.setCompoundDrawables(null, null, null, null);
    }




    public static void setPasswordVisible(EditText editText, int res) {
        showEditDrawable(editText, res);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    public static void setPasswordVisible(EditText editText, int res, int reqWidth, int reqHeight) {
        showEditDrawable(editText, res, reqWidth, reqHeight);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    public static void setPasswordInvisible(EditText editText, int res) {
        showEditDrawable(editText, res);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    public static void setPasswordInvisible(EditText editText, int res, int reqWidth, int reqHeight) {
        showEditDrawable(editText, res, reqWidth, reqHeight);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    public static boolean isPasswordVisible(EditText editText) {
        int inputType = editText.getInputType();
        final int variation = inputType & InputType.TYPE_MASK_VARIATION;
        return variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
    }

}
