package com.example.zhaolexi.imageloader.model;

import com.example.zhaolexi.imageloader.bean.Album;
import com.example.zhaolexi.imageloader.callback.OnLoadFinishListener;
import com.example.zhaolexi.imageloader.utils.Uri;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZHAOLEXI on 2018/2/5.
 */

public class GalleryModelImpl implements GalleryModel {
    @Override
    public List<Album> loadLocalHistory() {
        Connector.getDatabase();    //数据库不存在则创建数据库
        return DataSupport.findAll(Album.class);
    }

    @Override
    public void getRandom(final OnLoadFinishListener<List<Album>> listener) {
        final ArrayList<Album> albums = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    listener.onFail(e.getMessage());
                }
                Album album=new Album();
                album.setUrl(Uri.Girls);
                album.setTitle("福利");
                album.setAccessible(false);
                album.setAid("");
                albums.add(album);
                listener.onSuccess(albums);
            }
        }).start();
    }

    @Override
    public void addAlbumToDB(Album album) {
        album.save();
    }

    @Override
    public void removeAlbumFromDB(Album album) {
        album.delete();
    }
}
