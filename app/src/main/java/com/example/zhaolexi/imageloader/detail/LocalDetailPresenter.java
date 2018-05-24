package com.example.zhaolexi.imageloader.detail;

public class LocalDetailPresenter extends DetailPresenter<DetailViewInterface,DetailModel> {
    @Override
    protected DetailModel newModel() {
        return new DetailModelImpl();
    }
}
