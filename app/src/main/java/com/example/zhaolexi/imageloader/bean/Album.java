package com.example.zhaolexi.imageloader.bean;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by ZHAOLEXI on 2018/1/27.
 */

public class Album extends DataSupport implements Serializable{

    private static final long serialVersionUID=1L;

    //考虑到一些相册不一定有aid（比如第三方api），所以需要访问图片的url
    /*格式：
      我用的第三方api：http://gank.io/api/data/福利/10/
      云平台上的相册：http://119.29.39.173/gallery/Photo!data.action?pageSize=10&album.aid=‘aid’&currPage=
    */
    private String url;

    //用于后续对图片和相册的增、删、改进行操作
    private String aid;

    //相册名
    private String title;

    //相册的访问权限
    private boolean accessible;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof Album) {
            Album album=(Album) obj;
            if (url.equals(album.getUrl())) {
                return true;
            }
        }

        return false;
    }
}
