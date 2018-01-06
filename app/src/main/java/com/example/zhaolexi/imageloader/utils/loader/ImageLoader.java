package com.example.zhaolexi.imageloader.utils.loader;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.example.zhaolexi.imageloader.R;
import com.example.zhaolexi.imageloader.utils.MD5;
import com.example.zhaolexi.imageloader.utils.MyUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ImageLoader {

    private static final String TAG = "ImageLoader";


    /*
    默认线程池参数：为适应IO密集型操作，总线程数=核心线程数=2*CPU，采用无界的阻塞队列
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
    public static final Executor sDefaultExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), sThreadFactory);

    /*
    磁盘缓存参数：默认缓存容量为50M，节点数为1
     */
    private static long sDiskCacheSize = 1024 * 1024 * 50;
    private static final int DISK_CACHE_INDEX = 0;
    private boolean mIsDiskLruCacheCreated = false;

    /*
    内存缓存参数：默认容量为可用内存的1/8
     */
    private static int sMemoryCacheRate = 8;

    /*
    IO流用于缓冲区的数组大小为8k
     */
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final int TAG_KEY_URI = R.id.imageloader_uri;
    public static final int MESSAGE_POST_RESULT = 1;
    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //将加载完的图片显示到imageView上
                case MESSAGE_POST_RESULT:
                    LoaderResult result = (LoaderResult) msg.obj;
                    ImageView imageView = result.imageView;
                    //列表错位问题：滑太快的话图片加载完成要设置图片时已经设置到复用的item上
                    //解决办法，判断image的url有没有改变
                    String uri = (String) imageView.getTag(TAG_KEY_URI);
                    if (uri.equals(result.uri)) {
                        imageView.setImageBitmap(result.bitmap);
                    } else {
                        Log.w(TAG, "set image bitmap,but url has changed, ignored!");
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    };

    private Context mContext;
    private Executor mExecutor = sDefaultExecutor;
    private ImageResizer mImageResizer = new ImageResizer();
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;

    private ImageLoader(Context context) {
        mContext = context;

        //内存缓存，默认为可用内存的1/8
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / sMemoryCacheRate;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            //计算缓存对象大小
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };

        //磁盘缓存，默认为20M
        File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        if (getUsableSpace(diskCacheDir) > sDiskCacheSize) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1,
                        sDiskCacheSize);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "ImageLoader: insufficient memory when create diskCache!");
        }
    }

    /**
     * build a singleton of ImageLoader
     * you can choose to initialize a ThreadPool、MemoryCache or DiskCache
     */
    public static class Builder {
        private static volatile ImageLoader sInstance;

        public static ImageLoader build(Context context) {
            if (sInstance == null) {
                synchronized (ImageLoader.class) {
                    if (sInstance == null) {
                        sInstance = new ImageLoader(context);
                    }
                }
            }
            return sInstance;
        }

        private Builder initExecutor(ThreadPoolExecutor executor) {
            sInstance.mExecutor = executor;
            return this;
        }

        private Builder initMemoryCacheRate(int rate) {
            sInstance.sMemoryCacheRate = rate;
            return this;
        }

        private Builder initDiskCacheSize(long size) {
            sInstance.sDiskCacheSize = size;
            return this;
        }

    }

    /**
     * load bitmap from memory cache or disk cache or network async, then bind imageView and bitmap.
     * NOTE THAT: should run in UI Thread
     *
     * @param uri         http url/path
     * @param imageView   bitmap's bind object
     * @param taskOptions
     */
    public void bindBitmap(final String uri, final ImageView imageView, final TaskOptions taskOptions) {
        imageView.getWidth();
        imageView.setTag(TAG_KEY_URI, uri);
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        //如果内存缓存不存在就在线程池中调用loadBitmap方法
        Runnable loadBitmapTask = new Runnable() {

            @Override
            public void run() {
                Bitmap bitmap = null;
                try {
                    bitmap = loadBitmap(uri, taskOptions);
                } catch (SocketTimeoutException e) {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.image_fail);
                    if (taskOptions.handler != null) {
                        taskOptions.handler.onHandleSocketTimeout();
                    }
                }
                if (bitmap != null) {
                    LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };

        /*
        如果采用普通线程加载图片，随着列表的滑动可能会产生大量线程，影响整体效率
        不使用AsyncTask是因为3.0以上无法实现并发效果
         */
        mExecutor.execute(loadBitmapTask);

    }

    /**
     * only load bitmap from memory cache
     *
     * @param url
     * @return
     */
    public Bitmap loadBitmapFromMemCache(String url) {
        if (url == null) {
            return null;
        }
        final String key = MD5.digest(url);
        Bitmap bitmap = getBitmapFromMemCache(key);
        return bitmap;
    }

    /**
     * only load bitmap from disk（because bitmap from memory cache has been resized）
     * NOTE THAT: should run in UI Thread
     *
     * @param url
     * @param taskOptions   include reqWidth and reqHeight
     * @return
     * @throws IOException
     */
    public Bitmap loadBitmapFromDisk(String url, TaskOptions taskOptions) throws IOException {
        if(url==null) {
            return null;
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "load bitmap from UI Thread, it's not recommended!");
        }

        if (url.startsWith("/")) {
            FileInputStream fis = new FileInputStream(url);
            FileDescriptor fd = fis.getFD();
            return mImageResizer.decodeSampledBitmapFromFileDescriptor(fd,taskOptions);
        }

        if (mDiskLruCache == null) {
            return downloadBitmapFromUrl(url, taskOptions);
        }

        Bitmap bitmap = null;
        String key = MD5.digest(url);
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor,taskOptions);
        }

        return bitmap;
    }

    /**
     * only load bitmap from net
     *
     * @param urlString
     * @param taskOptions
     * @return
     * @throws SocketTimeoutException
     */
    public Bitmap downloadBitmapFromUrl(String urlString, TaskOptions taskOptions) throws SocketTimeoutException {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }

        if (urlString == null) {
            return null;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(urlString).build();
        BufferedInputStream in = null;
        Bitmap bitmap = null;
        try {
            Response response = client.newCall(request).execute();
            in = new BufferedInputStream(response.body().byteStream(), IO_BUFFER_SIZE);
            bitmap = mImageResizer.decodeSampledBitmapFromStream(in, taskOptions);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            MyUtils.close(in);
        }

        return bitmap;
    }

    /**
     * load bitmap from memory cache or disk cache or network.
     *
     * @param uri         http url
     * @param taskOptions
     * @return bitmap, maybe null.
     */
    private Bitmap loadBitmap(String uri, TaskOptions taskOptions) throws SocketTimeoutException {
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if (bitmap != null) {
            Log.d(TAG, "loadBitmapFromMemCache,url:" + uri);
            return bitmap;
        }

        //如果读取的是本地图片，则从sd卡中读取图片并缓存在内存中
        if (uri.startsWith("/")) {
            FileInputStream fis = null;
            FileDescriptor fd = null;
            try {
                fis = new FileInputStream(uri);
                fd = fis.getFD();
            } catch (IOException e) {
                e.printStackTrace();
            }

            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fd, taskOptions);
            addBitmapToMemoryCache(MD5.digest(uri), bitmap);
            Log.d(TAG, "loadBitmapFromDisk,url:" + uri);
            return bitmap;
        }

        try {
            bitmap = loadBitmapFromDiskCache(uri, taskOptions);
            if (bitmap != null) {
                Log.d(TAG, "loadBitmapFromDiskCache,url:" + uri);
                return bitmap;
            }
            bitmap = loadBitmapFromHttp(uri,taskOptions);
            Log.d(TAG, "loadBitmapFromHttp,url:" + uri);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }

        //因为磁盘缓存失效导致Bitmap没有加载
        if (bitmap == null && !mIsDiskLruCacheCreated) {
            Log.w(TAG, "encounter error, DiskLruCache is not created.");
            bitmap = downloadBitmapFromUrl(uri,taskOptions );
        }

        return bitmap;
    }


    /*
    addBitmapToMemoryCache  将图片添加到内存缓存
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (bitmap != null && getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /*
    getBitmapFromMemCache 从内存缓存中获取图片
     */
    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /*
    loadBitmapFromDiskCache 从磁盘缓存中获取图片，并将压缩后的图片添加到内存缓存
     */
    private Bitmap loadBitmapFromDiskCache(String url, TaskOptions taskOptions) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "load bitmap from UI Thread, it's not recommended!");
        }
        if (mDiskLruCache == null) {
            return null;
        }

        Bitmap bitmap = null;
        String key = MD5.digest(url);
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, taskOptions);
            if (bitmap != null) {
                //将压缩后的图片放入内存缓存
                addBitmapToMemoryCache(key, bitmap);
            }
        }

        return bitmap;
    }

    /*
    loadBitmapFromHttp  从网络中获取图片，并将图片添加到磁盘缓存
     */
    private Bitmap loadBitmapFromHttp(String url, TaskOptions taskOptions)
            throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        if (mDiskLruCache == null) {
            return null;
        }

        String key = MD5.digest(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);

        //将从网络中加载的原图放入磁盘缓存
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (downloadUrlToStream(url, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
        }

        return loadBitmapFromDiskCache(url,taskOptions);
    }

    private boolean downloadUrlToStream(String urlString,
                                        OutputStream outputStream) throws SocketTimeoutException {

        //连接超时的时间默认是10s，在这期间核心线程会被阻塞，新的任务会被添加到LinkedBrokingQueue中，
        //LinkedBrokingQueue默认长度是Integer.MAX_VALUE，所以新的任务会一直排队，导致即使想从磁盘中获取缓存
        //图片也还是会阻塞，所以将连接超时时间缩短为5s，并且对超时进行异常处理
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)   //SocketTimeOutException
                .build();
        Request request = new Request.Builder().url(urlString).build();
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            Response response = client.newCall(request).execute();
            in = new BufferedInputStream(response.body().byteStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;

        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            MyUtils.close(out);
            MyUtils.close(in);
        }
        return false;
    }

    /*
     * 获取缓存目录，如果sd卡可用就用外部存储，否则用内部存储
     * 内部存储在机身内存不足时会删除缓存
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        boolean externalStorageAvailable = Environment
                .getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }

        return new File(cachePath + File.separator + uniqueName);
    }

    /*
     * 返回此抽象路径名指定的分区上可用于此虚拟机的字节数
     */
    @TargetApi(VERSION_CODES.GINGERBREAD)
    private long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    /*
     * 对加载图片结果的封装
     */
    private static class LoaderResult {
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }
    }

    /**
     * 加载图片的选项。包括Options、超时处理、目标图片的尺寸和大小等
     */
    public static class TaskOptions {

        public BitmapFactory.Options options;
        public SocketTimeoutHanlder handler;

        /*
         * 若同时指定maxSize和reqWidth/reqHeight，则优先满足reqWidth/reqHeight
         */
        public int maxSize;

        /*
         * 图片的目标尺寸：
         * 指定长和宽，按比例缩放图片使图片大于或等于目标尺寸，并截取图片居中位置显示，相当于center_crop
         * 只指定长或宽，图片的目标尺寸将根据图片的比例计算得到，缩放规则同上
         * 如果都不指定，表示不对图片进行缩放
         */
        public int reqWidth;
        public int reqHeight;

        /*
         * 该参数只在只指定长或宽时有效，表示图片按比例缩放时的最小长度
         */
        public int minWidth;
        public int minHeight;

        public TaskOptions(int reqWidth, int reqHeight) {
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
        }

        public TaskOptions(int maxSize) {
            this.maxSize=maxSize*1024;
        }

        interface SocketTimeoutHanlder {
            void onHandleSocketTimeout();
        }
    }
}
