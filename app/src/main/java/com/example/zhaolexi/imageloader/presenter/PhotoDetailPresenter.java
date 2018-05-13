package com.example.zhaolexi.imageloader.presenter;

import com.example.zhaolexi.imageloader.model.DetailModel;
import com.example.zhaolexi.imageloader.model.PhotoDetailModelImpl;
import com.example.zhaolexi.imageloader.view.PhotoDetailViewInterface;

/**
 * Created by ZHAOLEXI on 2017/11/14.
 */

public class PhotoDetailPresenter extends DetailPresenter<PhotoDetailViewInterface> {

    @Override
    protected DetailModel newModel() {
        return new PhotoDetailModelImpl();
    }

    public void modifyDescription(String text) {

    }

    public void deletePhoto(int currentItem) {

    }
}
