package com.example.zhaolexi.imageloader.common.global;

public class Result<T> {

    public static final int SUCCESS = 1;
    public static final int FAIL = 0;
    public static final int TOKEN_ERROR = -1;
    public static final int ILLEGAL_ARGUMENT = -2;
    public static final int SERVER_ERROR = -3;
    public static final int PERMISSION_DENIED = -4;

    private int code;
    private String msg;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return code >= SUCCESS;
    }

}
