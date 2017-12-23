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
        Log.d(TAG, "src: " + src.getByteCount() / 1024);

        //进一步缩放Bitmap
        Bitmap dst = createScaledBitmap(src, taskOptions, taskOptions.options);

        Log.d(TAG, "dst: " + dst.getByteCount() / 1024);
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
        Log.d(TAG, "src: " + src.getByteCount() / 1024);

        //进一步缩放Bitmap
        Bitmap dst = createScaledBitmap(src, taskOptions, taskOptions.options);

        Log.d(TAG, "dst: " + dst.getByteCount() / 1024);
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
        Log.d(TAG, "src: " + src.getByteCount() / 1024);

        //进一步缩放Bitmap
        Bitmap dst = createScaledBitmap(src, taskOptions, taskOptions.options);

        Log.d(TAG, "dst: " + dst.getByteCount() / 1024);
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
        Log.d(TAG, "src: " + src.getByteCount() / 1024);

        //进一步缩放Bitmap
        Bitmap dst = createScaledBitmap(src, taskOptions, taskOptions.options);

        Log.d(TAG, "dst: " + dst.getByteCount() / 1024);
        return dst;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, ImageLoader.TaskOptions taskOptions) {

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (taskOptions.maxSize > 0) {
            //提取图片原始宽高信息

            int pixelByte = 0;
            switch (options.inPreferredConfig) {
                case ARGB_8888:
                    pixelByte = 4;
                    break;
                case ALPHA_8:
                    pixelByte = 1;
                    break;
                default:
                    pixelByte = 2;
            }
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight * halfWidth * pixelByte >= taskOptions.maxSize) {
                inSampleSize *= 2;
                halfHeight /= 2;
                halfWidth /= 2;
            }
        }

        if (taskOptions.reqWidth == 0 && taskOptions.reqHeight == 0) {
            return inSampleSize;
        }

        //当只给定高/宽时计算出按比例缩放后的宽/高
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
        Bitmap dst = src;

        if (src.getByteCount() <= taskOptions.maxSize) {
            return dst;
        }

        if (taskOptions.maxSize > 0) {
            Matrix matrix = new Matrix();
            float scale = (float) Math.sqrt((float) taskOptions.maxSize / (float) src.getByteCount());
            matrix.setScale(scale, scale);
            dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, false);
        }

        if ((taskOptions.reqHeight == 0 && taskOptions.reqWidth == 0)
                || (dst.getHeight() <= taskOptions.reqHeight || dst.getWidth() <= taskOptions.reqWidth)) {
            if (src != dst) {
                src.recycle();
            }
            return dst;
        }

        //目标图片可以由原图按比例缩放得到
        //由于采样率只能为2的幂，所以需要对图片进一步缩小
        //效果相当于fix_xy
        if ((float) taskOptions.reqHeight / (float) taskOptions.reqWidth == (float) dst.getHeight() / (float) dst.getWidth()) {
            dst = Bitmap.createScaledBitmap(dst, taskOptions.reqWidth, taskOptions.reqHeight, false);
        }

        //目标图片无法由原图按比例缩放得到
        //先对原图按比例缩小到View的长度/宽度，再截取图片居中显示
        //效果相当于center_crop
        else {
            Matrix matrix = new Matrix();
            float scale = Math.max((float) taskOptions.reqHeight / (float) dst.getHeight(), (float) taskOptions.reqWidth / (float) dst.getWidth());
            int dx = Math.round((taskOptions.reqWidth - dst.getWidth() * scale) / 2);
            int dy = Math.round((taskOptions.reqHeight - dst.getHeight() * scale) / 2);
            //scale缩小了图片的尺寸，减小了bitmap的大小
            matrix.setScale(scale, scale);
            //通过平移将bitmap居中显示。由于尺寸没有变化，所以bitmap大小不变
            matrix.postTranslate(dx, dy);
            dst = Bitmap.createBitmap(dst, 0, 0, dst.getWidth(), dst.getHeight(), matrix, false);
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
            int quality=100;
            while (baos.toByteArray().length > maxSize && quality > 0) {
                quality -= 10;
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
        }
        return baos.toByteArray();
    }

}
