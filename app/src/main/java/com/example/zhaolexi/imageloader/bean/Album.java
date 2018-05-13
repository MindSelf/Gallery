package com.example.zhaolexi.imageloader.bean;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.Date;

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

    private String aid;	//相册id
    private String title;	//相册名
    @SerializedName("isAccessible")
    private boolean accessible;	//权限标志
    private boolean isFavorite;		//收藏标志
    private Date createTime;	//创建时间
    private String adesc;	//相册描述
    private String who;	//创建者
    private String share;	//分享url
    private int total;	//相片总数
    private String coverUrl;	//封面url
    @SerializedName("number")
    private int account;  //相册号

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

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getAdesc() {
        return adesc;
    }

    public void setAdesc(String adesc) {
        this.adesc = adesc;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getAccount() {
        return account;
    }

    public void setAccount(int account) {
        this.account = account;
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
