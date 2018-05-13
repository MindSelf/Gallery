package com.example.zhaolexi.imageloader.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public class Photo implements Detail, Serializable {

    /**
     * _id : 59dd6a91421aa90fef20346c
     * createdAt : 2017-10-11T08:49:21.485Z
     * description : 10-11
     * publishedAt : 2017-10-11T12:40:42.545Z
     * source : chrome
     * type : 福利
     * thumbUrl : http://7xi8d6.com1.z0.glb.clouddn.com/20171011084856_0YQ0jN_joanne_722_11_10_2017_8_39_5_505.jpeg
     * used : true
     * who : 代码家
     */

    //private String id;
    @SerializedName(value = "pdesc", alternate = "desc")
    private String description;
    @SerializedName(value = "thumbUrl", alternate = "url")
    private String thumbUrl;
    private String fullUrl;
    private String who;

    private String publishAt;
    private int thumbUp;

    public String getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(String publishAt) {
        this.publishAt = publishAt;
    }

    public int getThumbUp() {
        return thumbUp;
    }

    public void setThumbUp(int thumbUp) {
        this.thumbUp = thumbUp;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    @Override
    public String getDetailUrl() {
        return fullUrl == null ? thumbUrl : fullUrl;
    }

    @Override
    public boolean shouldResized() {
        return fullUrl == null;
    }
}
