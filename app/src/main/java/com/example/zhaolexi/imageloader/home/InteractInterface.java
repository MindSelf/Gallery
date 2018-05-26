package com.example.zhaolexi.imageloader.home;

import android.support.v7.widget.RecyclerView;

public interface InteractInterface {

    RecyclerView.RecycledViewPool getRecycledViewPool();

    void setRecycledViewPool(RecyclerView.RecycledViewPool recycledViewPool);

    boolean canLoadWithoutWifi();

    void setCanLoadWithoutWifi(boolean canLoadWithoutWifi);

    /**
     *
     * @return should update managedAlbum state
     */
    boolean onShowManagerPage(int currentPos);

    void onDismissManagerPage(int currentPos);

    void setIsInManagePage(boolean isInManagePage);

    boolean isInManagePage();

    void changeCollectState(boolean isCollected);
}
