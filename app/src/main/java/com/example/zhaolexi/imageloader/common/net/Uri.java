package com.example.zhaolexi.imageloader.common.net;

/**
 * Created by ZHAOLEXI on 2017/10/14.
 */

public class Uri {

    //福利
    public static final String GIRLS = "http://gank.io/api/data/福利/10/";

    /*
    服务器地址
     */
    public static final String SERVER = "http://119.29.39.173/gallery/";

    /*
    Photo
     */
    //上传图片
    public static final String UPLOAD_IMG = SERVER + "Photo!batchUpload.action";
    //获取图片
    public static final String LOAD_IMG = SERVER + "Photo!photoData.action?pageSize=10";
    //编辑图片信息
    public static final String UPDATE_DESC = SERVER + "Photo!update.action?album.aid=%s&pid=%s&pdesc=%s";
    //删除图片
    public static final String DELETE_IMG = SERVER + "Photo!delete.action?album.aid=%s&pid=%s";
    //点赞
    public static final String TOGGLE_THUMB_UP = SERVER + "Photo!thumbUp.action?album.aid=%s&pid=%s";

    /*
    Album
     */
    //创建相册

    //添加相册
    public static final String ADD_ALBUM = SERVER + "Album!access.action?number=%s&readPassword=%s";
    //收藏相册
    public static final String COLLECT_ALBUM = SERVER + "Album!favorite.action?aid=";
    //获取收藏
    public static final String GET_COLLECT = SERVER + "Album!favoriteData.action?pageSize=10";
    //随便看看
    public static final String GET_RANDOM = SERVER + "Album!randomData.action";

    /*
    User
     */
    //手机号存在检测
    public static final String CHECK_MOBILE = SERVER + "User!existUser.action?uid=";
    //用户注册
    public static final String REGISTER = SERVER + "User!regist.action?uid=%s&uname=%s&upassword=%s";
    //用户登录
    public static final String LOGIN = SERVER + "User!login.action?uid=%s&upassword=%s";
}
