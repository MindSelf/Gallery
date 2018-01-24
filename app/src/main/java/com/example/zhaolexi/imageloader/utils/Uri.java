package com.example.zhaolexi.imageloader.utils;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public class Uri {

    //福利
    public static final String Girls ="http://gank.io/api/data/福利/10/";

    //服务器地址
    public static final String Server = "http://119.29.39.173/gallery/";

    //api

    //上传图片
    public static final String Upload_Img = Server + "Photo!batchUpload.action";
    //获取图片
    public static final String Load_Img = Server + "Photo!data.action?pageSize=10";
    //创建相册
    public static final String Add_Album = Server + "Album!add.action"+"?aname=%s&apassword=%s";
    //打开相册
    public static final String Open_Album = Server + "Album!open.action"+"?aname=%s&apassword=%s";
}
