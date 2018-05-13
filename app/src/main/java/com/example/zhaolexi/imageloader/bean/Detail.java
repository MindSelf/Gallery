package com.example.zhaolexi.imageloader.bean;

import java.io.Serializable;

public interface Detail extends Serializable{

    long serialVersionUID = 1L;

    String getDetailUrl();

    boolean shouldResized();
}
