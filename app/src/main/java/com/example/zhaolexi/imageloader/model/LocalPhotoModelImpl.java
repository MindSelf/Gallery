package com.example.zhaolexi.imageloader.model;

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
import com.example.zhaolexi.imageloader.presenter.SeletePhotoPresenter;
import com.example.zhaolexi.imageloader.utils.SharePreferencesUtils;
import com.example.zhaolexi.imageloader.utils.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

public class LocalPhotoModelImpl implements LocalPhotoModel {

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final int SUCCESS = 1;

    private OkHttpClient mClient;
    private Call mCall;
    private ContentResolver mContentResolver;
    private SparseArray<String> mThumbnailList;
    private SparseArray<PhotoBucket> mBucketList;
    private List<PhotoBucket> mBuckets;

    public LocalPhotoModelImpl() {
        mContentResolver = MyApplication.getContext().getContentResolver();
        mThumbnailList = new SparseArray<>();
        mBucketList = new SparseArray<>();
        mClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public List<PhotoBucket> getBuckets() {

        if (mBuckets == null) {
            getThumbnails();

            String[] projection = {Media._ID, Media.BUCKET_ID, Media.DATA, Media.BUCKET_DISPLAY_NAME};
            Cursor cursor = mContentResolver.query(Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
            int column_id = cursor.getColumnIndex(Media._ID);
            int column_bucket_id = cursor.getColumnIndex(Media.BUCKET_ID);
            int column_path = cursor.getColumnIndex(Media.DATA);
            int column_display_name = cursor.getColumnIndex(Media.BUCKET_DISPLAY_NAME);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(column_id);
                int bucket_id = cursor.getInt(column_bucket_id);
                String path = cursor.getString(column_path);
                String name = cursor.getString(column_display_name);

                PhotoBucket bucket = mBucketList.get(bucket_id);
                if (bucket == null) {
                    bucket = new PhotoBucket();
                    bucket.setName(name);
                    mBucketList.put(bucket_id, bucket);
                }
                Photo photo = new Photo();
                photo.setPath(path);
                photo.setThumbnailPath(mThumbnailList.get(id));
                bucket.getPhotoList().add(photo);
                bucket.setCount(bucket.getCount() + 1);
            }

            List<PhotoBucket> buckets = new ArrayList<>();

            PhotoBucket totalBucket = new PhotoBucket();
            totalBucket.setName("所有图片");
            buckets.add(totalBucket);
            int sum = 0;

            for (int i = 0; i < mBucketList.size(); i++) {
                int key = mBucketList.keyAt(i);
                PhotoBucket bucket = mBucketList.get(key);

                Bitmap cover = BitmapFactory.decodeFile(bucket.getPhotoList().get(0).getThumbnailPath());
                if (cover == null) {
                    cover = BitmapFactory.decodeFile(bucket.getPhotoList().get(0).getPath());
                }
                bucket.setCover(cover);

                buckets.add(bucket);

                totalBucket.getPhotoList().addAll(bucket.getPhotoList());
                sum += bucket.getCount();
            }

            totalBucket.setCount(sum);
            Bitmap cover = BitmapFactory.decodeFile(totalBucket.getPhotoList().get(0).getThumbnailPath());
            if (cover == null) {
                cover = BitmapFactory.decodeFile(totalBucket.getPhotoList().get(0).getPath());
            }
            totalBucket.setCover(cover);

            mBuckets = buckets;
        }
        return mBuckets;
    }

    @Override
    public void uploadImg(List<File> files, final SeletePhotoPresenter.OnUploadFinishListener listener) {

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (File file : files) {
            if (file != null) {
                //将File转化成输入流放入表单中
                builder.addFormDataPart("photos", file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
            }
        }
        //添加其他信息
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        builder.addFormDataPart("pdesc", format.format(new Date()));
        builder.addFormDataPart("who", "zlx");
        builder.addFormDataPart("album.aid", SharePreferencesUtils.getString(SharePreferencesUtils.Album, ""));

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
                Log.d("ZLX", "onFailure: " + e.getCause());
                if (!call.isCanceled()) {
                    listener.onUploadFinish(false, "服务器异常");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("test", "onResponse: " + response.toString());
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    int code = jsonObject.getInt("code");
                    String msg = jsonObject.getString("msg");
                    if(code==SUCCESS) {
                        listener.onUploadFinish(true, msg);
                    }else{
                        listener.onUploadFinish(false,msg);
                    }
                } catch (JSONException e) {
                    listener.onUploadFinish(false, "服务器异常");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean cancle() {
        if (mCall!=null&&!mCall.isCanceled()) {
            mCall.cancel();
            return true;
        } else {
            return false;
        }
    }

    private void getThumbnails() {
        String[] projection = {Thumbnails.IMAGE_ID, Thumbnails.DATA};
        Cursor cursor = mContentResolver.query(Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
        int column_id = cursor.getColumnIndex(Thumbnails.IMAGE_ID);
        int column_data = cursor.getColumnIndex(Thumbnails.DATA);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(column_id);
            String path = cursor.getString(column_data);
            mThumbnailList.put(id, path);
        }
        cursor.close();
    }
}
