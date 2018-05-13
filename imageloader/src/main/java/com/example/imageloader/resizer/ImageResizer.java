package com.example.imageloader.resizer;


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

    private static final BitmapFactory.Options sDefaultOptions = new BitmapFactory.Options();

    //注意，通过采样率来高效加载Bitmap对FileInputStream存在问题，因为FileInputSteam是有序的文件流，两次decodeStream会影响文件流的位置属性，导致第二次decodeStream返回null
    //所以需要将FileInputStream转换成FileDescriptor
    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, DecodeOption decodeOption) {

        if (decodeOption == null) {
            Log.d(TAG, "decodeSampledBitmapFromFileDescriptor: decode raw bitmap");
            return BitmapFactory.decodeFileDescriptor(fd, null, sDefaultOptions);
        }

        if (decodeOption.options == null) {
            decodeOption.options = sDefaultOptions;
        }

        // 计算采样率加载缩放过的Bitmap
        decodeOption.options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, decodeOption.options);
        decodeOption.options.inSampleSize = calculateInSampleSize(decodeOption.options, decodeOption);
        decodeOption.options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFileDescriptor(fd, null, decodeOption.options);

        Bitmap dst = null;
        if (src != null) {
            //由于采样率只能为2的幂，所以需要对Bitmap进一步缩放
            dst = createScaledBitmap(src, decodeOption);
            Log.d(TAG, "decodeSampledBitmapFromFileDescriptor: final size= " + dst.getByteCount() / 1024 + "kb，"
                    + "width = " + dst.getWidth() + "，height = " + dst.getHeight());
        }
        return dst;
    }

    //使用decodeStream会出现SkImageDecoder::Factory returned null
    public Bitmap decodeSampledBitmapFromByteArray(byte[] array, DecodeOption decodeOption) {

        if (decodeOption == null) {
            Log.d(TAG, "decodeSampledBitmapFromByteArray: decode raw bitmap");
            return BitmapFactory.decodeByteArray(array, 0, array.length, sDefaultOptions);
        }

        if (decodeOption.options == null) {
            decodeOption.options = sDefaultOptions;
        }

        // 计算采样率加载缩放过的Bitmap
        decodeOption.options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(array, 0, array.length, decodeOption.options);
        decodeOption.options.inSampleSize = calculateInSampleSize(decodeOption.options, decodeOption);
        decodeOption.options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeByteArray(array, 0, array.length, decodeOption.options);

        Bitmap dst = null;
        if (src != null) {
            //由于采样率只能为2的幂，所以需要对Bitmap进一步缩放
            dst = createScaledBitmap(src, decodeOption);
            Log.d(TAG, "decodeSampledBitmapFromByteArray: final size= " + dst.getByteCount() / 1024 + "kb，"
                    + "width = " + dst.getWidth() + "，height = " + dst.getHeight());
        }
        return dst;
    }

    public Bitmap decodeSampledBitmapFromStream(InputStream is, DecodeOption decodeOption) {

        if (decodeOption == null) {
            Log.d(TAG, "decodeSampledBitmapFromStream: decode raw bitmap");
            return BitmapFactory.decodeStream(is, null, sDefaultOptions);
        }

        if (decodeOption.options == null) {
            decodeOption.options = sDefaultOptions;
        }

        // 计算采样率加载缩放过的Bitmap
        decodeOption.options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, decodeOption.options);
        decodeOption.options.inSampleSize = calculateInSampleSize(decodeOption.options, decodeOption);
        decodeOption.options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeStream(is, null, decodeOption.options);

        Bitmap dst = null;
        if (src != null) {
            //由于采样率只能为2的幂，所以需要对Bitmap进一步缩放
            dst = createScaledBitmap(src, decodeOption);
            Log.d(TAG, "decodeSampledBitmapFromStream: final size= " + dst.getByteCount() / 1024 + "kb，"
                    + "width = " + dst.getWidth() + "，height = " + dst.getHeight());
        }
        return dst;
    }


    public Bitmap decodeSampledBitmapFromFile(String uri, DecodeOption decodeOption) {

        if (decodeOption == null) {
            Log.d(TAG, "decodeSampledBitmapFromFile: decode raw bitmap");
            return BitmapFactory.decodeFile(uri, sDefaultOptions);
        }

        if (decodeOption.options == null) {
            decodeOption.options = sDefaultOptions;
        }

        // 计算采样率加载缩放过的Bitmap
        decodeOption.options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, decodeOption.options);
        decodeOption.options.inSampleSize = calculateInSampleSize(decodeOption.options, decodeOption);
        decodeOption.options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(uri, decodeOption.options);

        Bitmap dst = null;
        if (src != null) {
            //由于采样率只能为2的幂，所以需要对Bitmap进一步缩放
            dst = createScaledBitmap(src, decodeOption);
            Log.d(TAG, "decodeSampledBitmapFromFile: final size= " + dst.getByteCount() / 1024 + "kb，"
                    + "width = " + dst.getWidth() + "，height = " + dst.getHeight());
        }
        return dst;
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res,
                                                  int resId, DecodeOption decodeOption) {

        if (decodeOption == null) {
            Log.d(TAG, "decodeSampledBitmapFromResource: decode raw bitmap");
            return BitmapFactory.decodeResource(res, resId, sDefaultOptions);
        }

        if (decodeOption.options == null) {
            decodeOption.options = sDefaultOptions;
        }

        // 计算采样率加载缩放过的Bitmap
        decodeOption.options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, decodeOption.options);
        decodeOption.options.inSampleSize = calculateInSampleSize(decodeOption.options, decodeOption);
        decodeOption.options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, decodeOption.options);

        Bitmap dst = null;
        if (src != null) {
            //由于采样率只能为2的幂，所以需要对Bitmap进一步缩放
            dst = createScaledBitmap(src, decodeOption);
            Log.d(TAG, "decodeSampledBitmapFromResource: final size= " + dst.getByteCount() / 1024 + "kb，"
                    + "width = " + dst.getWidth() + "，height = " + dst.getHeight());
        }
        return dst;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, DecodeOption decodeOption) {

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (decodeOption.reqWidth == 0 && decodeOption.reqHeight == 0) {
            return inSampleSize;
        }

        //当只给定高/宽时计算出按比例缩放后的宽/高
        //若有指定最小值，缩放后的结果不能小于最小值
        if (decodeOption.reqHeight == 0) {
            decodeOption.reqHeight = decodeOption.reqWidth * height / width;
        } else if (decodeOption.reqWidth == 0) {
            decodeOption.reqWidth = decodeOption.reqHeight * width / height;
        }

        if (height > decodeOption.reqHeight && width > decodeOption.reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            //图片的宽高不能比控件小
            while (halfHeight >= decodeOption.reqHeight && halfWidth >= decodeOption.reqWidth) {
                inSampleSize *= 2;
                halfHeight /= 2;
                halfWidth /= 2;
            }
        }

        Log.d(TAG, "calculateInSampleSize: " + inSampleSize);
        return inSampleSize;
    }


    private Bitmap createScaledBitmap(Bitmap src, DecodeOption decodeOption) {

        Bitmap dst = src;
        float scale = 1f;
        boolean isFromReqSize = false;

        //如果图片尺寸仍大于目标尺寸，则将图片压缩至目标尺寸大小，此时scale<1
        if (decodeOption.maxSize > 0 && src.getByteCount() > decodeOption.maxSize) {
            scale = (float) Math.sqrt((float) decodeOption.maxSize / (float) src.getByteCount());
        }

        //如果指定了reqHeight和reqWidth，如果scale<1，则屏蔽掉maxSize的结果
        if (decodeOption.reqHeight != 0 && decodeOption.reqWidth != 0) {
            float temp = 1;
            switch (decodeOption.scaleType) {
                case CENTER_CROP:
                    temp = Math.max((float) decodeOption.reqHeight / (float) src.getHeight(), (float) decodeOption.reqWidth / (float) src.getWidth());
                    break;
                case CENTER_INSIDE:
                    temp = Math.min((float) decodeOption.reqHeight / (float) src.getHeight(), (float) decodeOption.reqWidth / (float) src.getWidth());
                    break;
                default:
            }

            if (temp < 1) {
                scale = temp;
                isFromReqSize = true;
            }
        }

        if (scale < 1) {

            //将图片按比例缩放
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, false);

            //如果ScaleType是CENTER_CROP，则可以进一步压缩
            if (isFromReqSize && decodeOption.scaleType == DecodeOption.ScaleType.CENTER_CROP) {
                int x = Math.round((src.getWidth() * scale - decodeOption.reqWidth) / 2);
                int y = Math.round((src.getHeight() * scale - decodeOption.reqHeight) / 2);
                dst = Bitmap.createBitmap(dst, x, y, decodeOption.reqWidth, decodeOption.reqHeight);
            }
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
            Log.d(TAG, "compressImage: size= " + baos.toByteArray().length / 1024 + "kb");
        }
        return baos.toByteArray();
    }

}
