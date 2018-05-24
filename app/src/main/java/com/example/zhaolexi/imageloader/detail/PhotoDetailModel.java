package com.example.zhaolexi.imageloader.detail;

import com.example.zhaolexi.imageloader.common.net.OnRequestFinishListener;
import com.example.zhaolexi.imageloader.home.album.Photo;

public interface PhotoDetailModel extends DetailModel<Photo> {

    void setAid(String aid);

    void modifyDescription(String pid, String desc, OnRequestFinishListener listener);

    void deletePhoto(String pid, OnRequestFinishListener listener);

    void toggleThumbUp(String pid);

    void downloadImg(String url, String name, OnRequestFinishListener listener);

}
