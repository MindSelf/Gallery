package com.example.imageloader.imageloader;

import android.support.annotation.NonNull;

public abstract class PriorityRunnable implements Comparable<PriorityRunnable>, Runnable {

    private int priority;

    PriorityRunnable(int priority) {
        this.priority = priority;
    }

    PriorityRunnable() {
        this(0);
    }

    @Override
    public int compareTo(@NonNull PriorityRunnable o) {
        return Integer.compare(priority, o.priority);
    }
}