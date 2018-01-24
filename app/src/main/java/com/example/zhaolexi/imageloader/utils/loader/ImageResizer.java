package com.example.zhaolexi.imageloader.utils.loader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * 图片压缩
 */
public class ImageResizer {
    private static final String TAG = "ImageResizer";

    //注意，通过采样率来高效加载Bitmap对FileInputStream存在问题，因为FileInputSteam是有序的文件流，两次decodeStream会影响文件流的位置属性，导致第二次decodeStream返回null
    //所以需要将FileInputStream转换成FileDescriptor
    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, ImageLoader.TaskOptions taskOptions) {

        if (taskOptions.options == null) {
            taskOptions.options = new BitmapFactory.Options();
        }

        // 计算采样率加载缩放过的Bitmap
        taskOptions.options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, taskOptions.options);
        taskOptions.options.inSampleSize = calculateInSampleSize(taskOptions.options, taskOptions);
        taskOptions.options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFileDescriptor(fd, null, taskOptions.options);

        Bitmap dst = null;
        if (src != null) {
            Log.d(TAG, "src: " + src.getByteCount() / 1024);
            //由于采样率只能为2的幂，所以需要对Bitmap进一步缩放
            dst = createScaledBitmap(src, taskOptions, taskOptions.options);

            Log.d(TAG, "dst: " + dst.getByteCount() / 1024);
        }
        return dst;
    }

    public Bitmap decodeSampledBitmapFromStream(InputStream is, ImageLoader.TaskOptions taskOptions) {

        if (taskOptions.options == null) {
            taskOptions.options = new BitmapFactory.Options();
        }

        // 计算采样率加载缩放过的Bitmap
        taskOptions.options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, taskOptions.options);
        taskOptions.options.inSampleSize = calculateInSampleSize(taskOptions.options, taskOptions);
        taskOptions.options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeStream(is, null, taskOptions.options);

        Bitmap dst = null;
        if (src != null) {
            Log.d(TAG, "src: " + src.getByteCount() / 1024);
            //由于采样率只能为2的幂，所以需要对Bitmap进一步缩放
            dst = createScaledBitmap(src, taskOptions, taskOptions.options);

            Log.d(TAG, "dst: " + dst.getByteCount() / 1024);
        }
        return dst;
    }

    public Bitmap decodeSampledBitmapFromFile(String uri, ImageLoader.TaskOptions taskOptions) {

        if (taskOptions.options == null) {
            taskOptions.options = new BitmapFactory.Options();
        }

        // 计算采样率加载缩放过的Bitmap
        taskOptions.options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, taskOptions.options);
        taskOptions.options.inSampleSize = calculateInSampleSize(taskOptions.options, taskOptions);
        taskOptions.options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(uri, taskOptions.options);

        Bitmap dst = null;
        if (src != null) {
            Log.d(TAG, "src: " + src.getByteCount() / 1024);
            //由于采样率只能为2的幂，所以需要对Bitmap进一步缩放
            dst = createScaledBitmap(src, taskOptions, taskOptions.options);

            Log.d(TAG, "dst: " + dst.getByteCount() / 1024);
        }
        return dst;
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res,
                                                  int resId, ImageLoader.TaskOptions taskOptions) {

        if (taskOptions.options == null) {
            taskOptions.options = new BitmapFactory.Options();
        }

        // 计算采样率加载缩放过的Bitmap
        taskOptions.options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, taskOptions.options);
        taskOptions.options.inSampleSize = calculateInSampleSize(taskOptions.options, taskOptions);
        taskOptions.options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, taskOptions.options);

        Bitmap dst = null;
        if (src != null) {
            Log.d(TAG, "src: " + src.getByteCount() / 1024);
            //由于采样率只能为2的幂，所以需要对Bitmap进一步缩放
            dst = createScaledBitmap(src, taskOptions, taskOptions.options);

            Log.d(TAG, "dst: " + dst.getByteCount() / 1024);
        }
        return dst;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, ImageLoader.TaskOptions taskOptions) {

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (taskOptions.reqWidth == 0 && taskOptions.reqHeight == 0) {
            return inSampleSize;
        }

        //当只给定高/宽时计算出按比例缩放后的宽/高
        //若有指定最小值，缩放后的结果不能小于最小值
        if (taskOptions.reqHeight == 0) {
            taskOptions.reqHeight = taskOptions.reqWidth * height / width;
        } else if (taskOptions.reqWidth == 0) {
            taskOptions.reqWidth = taskOptions.reqHeight * width / height;
        }

        //重新计算图片的高宽
        height = height / inSampleSize;
        width = width / inSampleSize;

        if (height > taskOptions.reqHeight && width > taskOptions.reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            //图片的宽高不能比控件小
            while (halfHeight >= taskOptions.reqHeight && halfWidth >= taskOptions.reqWidth) {
                inSampleSize *= 2;
                halfHeight /= 2;
                halfWidth /= 2;
            }
        }

        return inSampleSize;
    }


    private Bitmap createScaledBitmap(Bitmap src, ImageLoader.TaskOptions taskOptions, BitmapFactory.Options options) {

        if (taskOptions.reqHeight == 0 && taskOptions.reqWidth == 0 && (taskOptions.maxSize <= 0 || src.getByteCount() <= taskOptions.maxSize)) {
            return src;
        }

        Bitmap dst;
        float scale = 1f;

        //如果图片尺寸仍大于目标尺寸，则将图片压缩至目标尺寸大小
        if (taskOptions.maxSize > 0 && src.getByteCount() > taskOptions.maxSize) {
            scale = (float) Math.sqrt((float) taskOptions.maxSize / (float) src.getByteCount());
        }

        //若同时指定maxSize和reqWidth/reqHeight，则优先满足reqWidth/reqHeight
        if (!(taskOptions.reqHeight == 0 && taskOptions.reqWidth == 0)) {
            switch (taskOptions.scaleType) {
                case CENTER_CROP:
                    scale = Math.max((float) taskOptions.reqHeight / (float) src.getHeight(), (float) taskOptions.reqWidth / (float) src.getWidth());
                    break;
                case CENTER_INSIDE:
                    scale = Math.min((float) taskOptions.reqHeight / (float) src.getHeight(), (float) taskOptions.reqWidth / (float) src.getWidth());
                    break;
                default:
            }
        }

        //将图片按比例缩放
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, false);

        if (taskOptions.scaleType == ImageLoader.TaskOptions.ScaleType.CENTER_CROP) {
            //如果偏移量x和y不为0，表示目标图片无法由原图按比例缩放得到，需要截取图片居中部分显示
            int x = Math.round((src.getWidth() * scale - taskOptions.reqWidth) / 2);
            int y = Math.round((src.getHeight() * scale - taskOptions.reqHeight) / 2);
            dst = Bitmap.createBitmap(dst, x, y, taskOptions.reqWidth, taskOptions.reqHeight);
        }

        if (dst != src) {
            src.recycle();
        }
        return dst;
    }

    public static byte[] compressImage(Bitmap bitmap, int maxSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length > maxSize) {
            int quality = 100;
            while (baos.toByteArray().length > maxSize && quality > 0) {
                quality -= 10;
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
        }
        return baos.toByteArray();
    }

}
