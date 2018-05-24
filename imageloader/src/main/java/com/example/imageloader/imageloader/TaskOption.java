package com.example.imageloader.imageloader;

import com.example.imageloader.resizer.DecodeOption;

public class TaskOption {

    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MIDDLE = 5;
    public static final int PRIORITY_HIGH = 10;

    public DecodeOption decodeOption;
    public int priority;

    public TaskOption(DecodeOption decodeOption, int priority) {
        this.decodeOption = decodeOption;
        this.priority = priority;
    }

    public TaskOption(DecodeOption decodeOption) {
        this(decodeOption, 0);
    }
}
