package com.example.zhaolexi.imageloader.model;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.util.SparseArray;

import com.example.zhaolexi.imageloader.base.MyApplication;
import com.example.zhaolexi.imageloader.bean.Photo;
import com.example.zhaolexi.imageloader.bean.PhotoBucket;
import com.example.zhaolexi.imageloader.callback.OnUploadFinishListener;
import com.example.zhaolexi.imageloader.utils.MyUtils;
import com.example.zhaolexi.imageloader.utils.SharePreferencesUtils;
import com.example.zhaolexi.imageloader.utils.Uri;
import com.example.zhaolexi.imageloader.utils.loader.ImageLoader;
import com.example.zhaolexi.imageloader.utils.loader.ImageResizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public class UploadPhotoModelImpl implements UploadPhotoModel {

    private static final String TAG = "UploadPhotoModelImpl";
    private AtomicInteger totalsize = new AtomicInteger(0);

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final int SUCCESS = 1;
    private static final int MAX_UPLOAD_SIZE = 2 * 1024 * 1024;

    private ImageLoader mImageLoader;
    private OkHttpClient mClient;
    private Call mCall;
    private ContentResolver mContentResolver;
    private SparseArray<String> mThumbnails; //缩略图
    private SparseArray<PhotoBucket> mBuckets;   //相册id与相册的map
    private List<PhotoBucket> mBucketList;    //相册的list
    private int mEdge;  //用于对图片的尺寸进行压缩
    private String mUploadAid;

    public UploadPhotoModelImpl() {
        mContentResolver = MyApplication.getContext().getContentResolver();
        mImageLoader = new ImageLoader.Builder(MyApplication.getContext()).build();
        mThumbnails = new SparseArray<>();
        mBuckets = new SparseArray<>();
        mClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .build();
        mEdge = MyUtils.dp2px(MyApplication.getContext(), 80);
    }

    @Override
    public void setAid(String aid) {
        mUploadAid = aid;
    }

    /**
     * 获取系统相册
     *
     * @return 相册的list
     */
    @Override
    public List<PhotoBucket> getBuckets() {

        //第一次获取系统相册时创建mBuckets
        if (mBucketList == null) {
            mBucketList = new ArrayList<>();

            getThumbnails();

            String[] projection = {Media._ID, Media.BUCKET_ID, Media.DATA, Media.BUCKET_DISPLAY_NAME};
            Cursor cursor = mContentResolver.query(Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
            if (cursor!=null) {
                int column_id = cursor.getColumnIndex(Media._ID);
                int column_bucket_id = cursor.getColumnIndex(Media.BUCKET_ID);
                int column_path = cursor.getColumnIndex(Media.DATA);
                int column_display_name = cursor.getColumnIndex(Media.BUCKET_DISPLAY_NAME);
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(column_id);
                    int bucket_id = cursor.getInt(column_bucket_id);
                    String path = cursor.getString(column_path);
                    String name = cursor.getString(column_display_name);

                    //相册名相同的照片放在同一个Bucket中

                    PhotoBucket bucket = mBuckets.get(bucket_id);
                    //如果相册不存在则向mBuckets中添加一个Bucket
                    if (bucket == null) {
                        bucket = new PhotoBucket();
                        bucket.setName(name);
                        mBuckets.put(bucket_id, bucket);
                    }
                    Photo photo = new Photo();
                    photo.setPath(path);
                    photo.setThumbnailPath(mThumbnails.get(id));
                    bucket.getPhotoSet().add(photo);
                    bucket.setCount(bucket.getCount() + 1);
                }
                cursor.close();
            }

            final PhotoBucket totalBucket = new PhotoBucket();
            totalBucket.setName("所有图片");
            mBucketList.add(totalBucket);
            int sum = 0;

            for (int i = 0; i < mBuckets.size(); i++) {
                int key = mBuckets.keyAt(i);
                final PhotoBucket bucket = mBuckets.get(key);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap cover = null;
                        try {
                            Iterator<Photo> iterator = bucket.getPhotoSet().iterator();
                            Photo mFirst = null;
                            if (iterator.hasNext()) mFirst = iterator.next();
                            if (mFirst != null) {
                                cover = mImageLoader.loadBitmapFromDisk(mFirst.getThumbnailPath(), new ImageLoader.TaskOptions(mEdge, mEdge));
                                if (cover == null) {
                                    //Unable to decode stream or ThumbnailPath is null
                                    cover = mImageLoader.loadBitmapFromDisk(mFirst.getPath(), new ImageLoader.TaskOptions(mEdge, mEdge));
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        bucket.setCover(cover);
                        Log.d(TAG, "cover: " + cover.getByteCount() / 1024);
                        totalsize.addAndGet(cover.getByteCount());
                    }
                }).start();

                mBucketList.add(bucket);

                totalBucket.getPhotoSet().addAll(bucket.getPhotoSet());
                sum += bucket.getCount();
            }

            totalBucket.setCount(sum);

            if (sum > 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap cover = null;
                        try {
                            Iterator<Photo> iterator = totalBucket.getPhotoSet().iterator();
                            Photo mFirst = null;
                            if (iterator.hasNext()) mFirst = iterator.next();
                            if (mFirst != null) {
                                cover = mImageLoader.loadBitmapFromDisk(mFirst.getThumbnailPath(), new ImageLoader.TaskOptions(mEdge, mEdge));
                                if (cover == null) {
                                    //Unable to decode stream or ThumbnailPath is null
                                    cover = mImageLoader.loadBitmapFromDisk(mFirst.getPath(), new ImageLoader.TaskOptions(mEdge, mEdge));
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        totalBucket.setCover(cover);
                        Log.d(TAG, "cover: " + cover.getByteCount() / 1024);
                        totalsize.addAndGet(cover.getByteCount());
                    }
                }).start();
            }

        }
        Log.d(TAG, "totalsize: " + totalsize.get() / 1024);
        return mBucketList;
    }


    /**
     * 上传图片
     *
     * @param files    所要上传的图片文件
     * @param listener 回调接口
     */
    @Override
    public void uploadImg(List<File> files, final OnUploadFinishListener listener) {

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (File file : files) {
            if (file != null) {
                //将File转化成输入流放入表单中
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                builder.addFormDataPart("photos", file.getName(), RequestBody.create(MEDIA_TYPE_PNG, ImageResizer.compressImage(bitmap, MAX_UPLOAD_SIZE)));
            }
        }
        //添加其他信息
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        builder.addFormDataPart("pdesc", format.format(new Date()));
        builder.addFormDataPart("who", SharePreferencesUtils.getString(SharePreferencesUtils.User, "zlx"));
        builder.addFormDataPart("album.aid", mUploadAid);

        MultipartBody requestBody = builder.build();

        //构建请求
        Request request = new Request.Builder()
                .url(Uri.Upload_Img)//地址
                .post(requestBody)//添加请求体
                .build();
        mCall = mClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Upload", "onFailure: " + e);
                if (!call.isCanceled()) {
                    listener.onUploadFinish(false, "服务器异常");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("Upload", "onResponse: " + response.toString());
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    int code = jsonObject.getInt("code");
                    String msg = jsonObject.getString("msg");
                    if (code == SUCCESS) {
                        listener.onUploadFinish(true, msg);
                    } else {
                        listener.onUploadFinish(false, msg);
                    }
                } catch (JSONException e) {
                    listener.onUploadFinish(false, "数据异常");
                }
            }
        });
    }

    /**
     * 取消图片上传
     *
     * @return 取消上传任务是否成功
     */
    @Override
    public boolean cancel() {
        if (mCall != null && !mCall.isCanceled()) {
            mCall.cancel();
            return true;
        } else {
            return false;
        }
    }

    /*
    获取手机中的缩略图
     */
    private void getThumbnails() {
        String[] projection = {Thumbnails.IMAGE_ID, Thumbnails.DATA};
        Cursor cursor = mContentResolver.query(Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
        if (cursor!=null) {
            int column_id = cursor.getColumnIndex(Thumbnails.IMAGE_ID);
            int column_data = cursor.getColumnIndex(Thumbnails.DATA);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(column_id);
                String path = cursor.getString(column_data);
                mThumbnails.put(id, path);
            }
            cursor.close();
        }
    }
}
