package com.example.zhaolexi.imageloader.me.album.list.favorite;

import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {

    private static List<CollectionListener> mListeners = new ArrayList<>();

    public static void addCollectionListener(CollectionListener listener){
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public static void removeCollectionListener(CollectionListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    public static void notifyCollectionCancel(String aid) {
        for (CollectionListener listener : mListeners) {
            listener.onCancel(aid);
        }
    }
}
