package com.example.imageloader.thread;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoaderExecutor extends ThreadPoolExecutor {

    /**
     * 适应IO密集型操作的默认线程池：
     * 总线程数：CORE_POOL_SIZE = 2CPU
     * 核心线程数：CORE_POOL_SIZE = 2CPU
     * 非核心线程：0  存活时间：KEEP_ALIVE=5 SECONDS
     * 阻塞队列：采用无界的带有优先级的阻塞队列PriorityBlockingQueue
     * 工厂方法：创建线程时为其命名
     */

    private static final int CPU_COUNT = Runtime.getRuntime()
            .availableProcessors();

    private static final int CORE_POOL_SIZE = 2 * CPU_COUNT;
    private static final int MAXIMUM_POOL_SIZE = 2 * CPU_COUNT;
    private static final long KEEP_ALIVE = 5L;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
        }
    };

    public ImageLoaderExecutor() {
        super(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
                new PriorityBlockingQueue<Runnable>(), sThreadFactory);
    }
}
