package com.example.zhaolexi.imageloader.detail;

import android.graphics.Bitmap;
import android.os.Environment;

import com.example.imageloader.imageloader.ImageLoader;
import com.example.imageloader.imageloader.ImageLoaderConfig;
import com.example.imageloader.imageloader.TaskOption;
import com.example.imageloader.resizer.DecodeOption;
import com.example.imageloader.resizer.ImageResizer;
import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.redirect.router.Result;
import com.example.zhaolexi.imageloader.common.net.DefaultCallback;
import com.example.zhaolexi.imageloader.redirect.login.DefaultCookieJar;
import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.common.net.SendCallback;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.home.album.Photo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PhotoDetailModelImpl implements PhotoDetailModel {

    private final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/share_gallery/";
    private String mAid;
    private String mUrl;

    private OkHttpClient mClient;

    public PhotoDetailModelImpl() {
        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
        ImageLoaderConfig config = new ImageLoaderConfig.Builder(BaseApplication.getContext())
                .setFailImage(0).build();
        ImageLoader.getInstance(BaseApplication.getContext()).init(config);
    }

    @Override
    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public void setAid(String aid) {
        mAid = aid;
    }

    @Override
    public void loadMoreData(int page, final OnRequestFinishListener<List<Photo>> listener) {
        Request request = new Request.Builder().url(mUrl + page).build();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFail(e.getMessage(), null);
            }

            @Override
            public void onResponse(Call call, Response response) {

                Result<List<Photo>> result = new Result<>();
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = null;
                    if (jsonObject.has("error")) {
                        //第三方相册
                        boolean error = jsonObject.getBoolean("error");
                        jsonArray = jsonObject.getJSONArray("results");
                        result.setCode(error ? Result.SERVER_ERROR : Result.SUCCESS);
                    } else if (jsonObject.has("code")) {
                        //gallery album
                        jsonArray = jsonObject.getJSONObject("data").getJSONArray("results");
                        result.setCode(jsonObject.getInt("code"));
                        result.setMsg(jsonObject.getString("msg"));
                    }
                    if (jsonArray != null) {
                        List<Photo> list = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<Photo>>() {
                        }.getType());
                        result.setData(list);
                    }

                } catch (JSONException | IOException e) {
                    listener.onFail(BaseApplication.getContext().getString(R.string.json_error), null);
                }

                if (result.isSuccess()) {
                    listener.onSuccess(result.getData());
                } else {
                    listener.onFail("获取数据失败", result);
                }
            }
        });
    }


    @Override
    public void modifyDescription(String pid, String desc, final OnRequestFinishListener listener) {
        Request request = new Request.Builder().url(String.format(Uri.UPDATE_DESC, mAid, pid, desc)).build();
        mClient.newCall(request).enqueue(new SendCallback(listener));
    }

    @Override
    public void deletePhoto(String pid, OnRequestFinishListener listener) {
        Request request = new Request.Builder().url(String.format(Uri.DELETE_IMG, mAid, pid)).build();
        mClient.newCall(request).enqueue(new SendCallback(listener));
    }

    @Override
    public void toggleThumbUp(String pid) {
        Request request = new Request.Builder().url(String.format(Uri.TOGGLE_THUMB_UP, mAid, pid)).build();
        mClient.newCall(request).enqueue(new DefaultCallback());
    }

    @Override
    public void downloadImg(final String url, final String name, final OnRequestFinishListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TaskOption taskOption = new TaskOption(new DecodeOption(0, 0), TaskOption.PRIORITY_HIGH);
                Bitmap bitmap = ImageLoader.getInstance(BaseApplication.getContext()).loadBitmap(url, taskOption);
                if (bitmap != null) {
                    try {
                        ImageResizer.saveBitmapToFile(bitmap, getFile(name));
                        listener.onSuccess(null);
                    } catch (FileNotFoundException e) {
                        listener.onFail("保存图片失败", null);
                    }
                } else {
                    listener.onFail("下载图片失败", null);
                }
            }
        }).start();
    }

    private File getFile(String name) {
        File directory = new File(dir);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(dir, name + ".jpg");
        return file;
    }

}
