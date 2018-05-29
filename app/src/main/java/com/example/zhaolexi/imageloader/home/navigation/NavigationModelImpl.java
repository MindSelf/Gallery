package com.example.zhaolexi.imageloader.home.navigation;

import com.example.imageloader.imageloader.ImageLoader;
import com.example.imageloader.imageloader.ImageLoaderConfig;
import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.common.base.BaseApplication;
import com.example.zhaolexi.imageloader.common.net.Uri;
import com.example.zhaolexi.imageloader.common.utils.AlbumConstructor;
import com.example.zhaolexi.imageloader.home.manager.Album;
import com.example.zhaolexi.imageloader.redirect.login.DefaultCookieJar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NavigationModelImpl implements NavigationModel {

    private static final String URI_MY_ALBUM = Uri.MY_ALBUM + "&currPage=1";
    private static final String URI_FAVORITE_ALBUM = Uri.FAVORITE_ALBUM + "&currPage=1";

    private List<Album> mMyAlbumList;
    private List<Album> mFavoriteAlbumList;
    private OkHttpClient mClient;
    private Call mMyAlbumCall, mFavoriteAlbumCall;

    public NavigationModelImpl() {
        mClient = new OkHttpClient.Builder().cookieJar(new DefaultCookieJar()).build();
    }

    @Override
    public void loadAlbumInfo() {
        mMyAlbumList = new ArrayList<>();
        mFavoriteAlbumList = new ArrayList<>();

        ImageLoaderConfig config = new ImageLoaderConfig.Builder(BaseApplication.getContext())
                .setFailImage(0).build();
        ImageLoader.getInstance(BaseApplication.getContext()).init(config);

        //预加载数据
        Request myAlbum = new Request.Builder().url(URI_MY_ALBUM).build();
        mMyAlbumCall = mClient.newCall(myAlbum);
        mMyAlbumCall.enqueue(new PreLoadCallback(mMyAlbumList));

        Request favoriteAlbum = new Request.Builder().url(URI_FAVORITE_ALBUM).build();
        mFavoriteAlbumCall = mClient.newCall(favoriteAlbum);
        mFavoriteAlbumCall.enqueue(new PreLoadCallback(mFavoriteAlbumList));
    }

    @Override
    public void releaseAlbumInfo() {
        //释放数据
        mMyAlbumList = null;
        mFavoriteAlbumList = null;

        mMyAlbumCall.cancel();
        mFavoriteAlbumCall.cancel();

        ImageLoaderConfig config = new ImageLoaderConfig.Builder(BaseApplication.getContext())
                .setDefaultImage(R.color.windowBackground)
                .setFailImage(R.mipmap.image_fail).build();
        ImageLoader.getInstance(BaseApplication.getContext()).init(config);
    }

    @Override
    public List<Album> getMyAlbums() {
        return mMyAlbumList;
    }

    @Override
    public List<Album> getFavoriteAlbums() {
        return mFavoriteAlbumList;
    }

    private void onHandleData(List<Album> albums) {
        AlbumConstructor constructor = new AlbumConstructor();
        for (Album album : albums) {
            constructor.construct(album);
            //预加载第一页的cover
            constructor.setCover(album);
        }
    }

    private class PreLoadCallback implements Callback {

        private List<Album> mList;

        public PreLoadCallback(List<Album> list) {
            mList = list;
        }

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                JSONObject resp = new JSONObject(response.body().string());
                if (resp.optInt("code") == 1 && resp.has("data")) {
                    JSONObject data = resp.optJSONObject("data");
                    JSONArray result = data.optJSONArray("results");
                    if (result != null) {
                        List<Album> albums = new Gson().fromJson(result.toString(), new TypeToken<List<Album>>() {
                        }.getType());
                        if (mList != null && albums != null) {
                            onHandleData(albums);
                            mList.clear();
                            mList.addAll(albums);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
