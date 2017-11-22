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

    private static volatile ImageLoader instance;

    private static final String TAG = "ImageLoader";

    public static final int MESSAGE_POST_RESULT = 1;

    /*
    线程池参数：核心线程数为2*CPU核心数，线程限制超时时长10s
     */
    private static final int CPU_COUNT = Runtime.getRuntime()
            .availableProcessors();
    private static final int CORE_POOL_SIZE = 2*CPU_COUNT ;
    private static final int MAXIMUM_POOL_SIZE = 2*CPU_COUNT;
    private static final long KEEP_ALIVE = 5L;

    /*
    磁盘缓存参数：缓存容量为50M，节点数为1
     */
//    private static final int TAG_KEY_LOADING=R.id.imageloader_loading;
    private static final int TAG_KEY_URI = R.id.imageloader_uri;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int DISK_CACHE_INDEX = 0;
    private boolean mIsDiskLruCacheCreated = false;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), sThreadFactory);

    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.imageView;
            String uri = (String) imageView.getTag(TAG_KEY_URI);
            if (uri.equals(result.uri)) {
                imageView.setImageBitmap(result.bitmap);
            } else {
                //列表错位问题：滑太快的话图片加载完成要设置图片时已经设置到复用的item上
                //解决办法，判断image的url有没有改变
                Log.w(TAG, "set image bitmap,but url has changed, ignored!");
            }
        }
    };

    private Context mContext;
    private ImageResizer mImageResizer = new ImageResizer();
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;

    private ImageLoader(Context context) {
        mContext = context.getApplicationContext();
        //缓存空间为当前可用内存的1/8，单位kb
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 4;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            //计算缓存对象大小
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };

        File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1,
                        DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //机身内存不足，磁盘缓存失效
    }

    /**
     * getInstance a new instance of ImageLoader
     * @param context
     * @return a new instance of ImageLoader
     */
    public static ImageLoader getInstance(Context context) {
        if(instance==null){
            synchronized (ImageLoader.class) {
                if(instance==null){
                    instance = new ImageLoader(context);
                }
            }
        }
        return instance;
    }


    /**
     * load bitmap from memory cache or disk cache or network async, then bind imageView and bitmap.
     * NOTE THAT: should run in UI Thread
     * @param uri http url
     * @param imageView bitmap's bind object
     */
    public void bindBitmap(final String uri, final ImageView imageView) {
        bindBitmap(uri, imageView, 0, 0);
    }

    public void bindBitmap(final String uri, final ImageView imageView,
            final int reqWidth, final int reqHeight) {
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
                    bitmap = loadBitmap(uri, reqWidth, reqHeight);
                } catch (SocketTimeoutException e) {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image_fail);
                }
                if (bitmap != null) {
                    LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);

        /*
        如果采用普通线程加载图片，随着列表的滑动可能会产生大量线程，影响整体效率
        不使用AsyncTask是因为3.0以上无法实现并发效果
         */
    }

    public Bitmap loadRawBitmap(String url,int reqWidth,int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "load bitmap from UI Thread, it's not recommended!");
        }

        if (url.startsWith("/")) {
            return mImageResizer.decodeSampledBitmapFromFile(url, reqWidth, reqHeight);
        }

        if (mDiskLruCache == null) {
            return null;
        }

        Bitmap bitmap = null;
        String key = MD5.digest(url);
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream)snapShot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
        }

        return bitmap;
    }


    /**
     * load bitmap from memory cache or disk cache or network.
     * @param uri http url
     * @param reqWidth the width ImageView desired
     * @param reqHeight the height ImageView desired
     * @return bitmap, maybe null.
     */
    private Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) throws SocketTimeoutException {
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if (bitmap != null) {
            Log.d(TAG, "loadBitmapFromMemCache,url:" + uri);
            return bitmap;
        }

        if(uri.startsWith("/")){
            bitmap = mImageResizer.decodeSampledBitmapFromFile(uri, reqWidth, reqHeight);
            addBitmapToMemoryCache(MD5.digest(uri), bitmap);
            return bitmap;
        }

        try {
            bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
            if (bitmap != null) {
                Log.d(TAG, "loadBitmapFromDiskCache,url:" + uri);
                return bitmap;
            }
            bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
            Log.d(TAG, "loadBitmapFromHttp,url:" + uri);
        } catch (IOException e) {
            if(e instanceof SocketTimeoutException)
                throw (SocketTimeoutException)e;
            else
                e.printStackTrace();
        }

        //因为磁盘缓存失效导致Bitmap没有加载
        if (bitmap == null && !mIsDiskLruCacheCreated) {
            Log.w(TAG, "encounter error, DiskLruCache is not created.");
            bitmap = downloadBitmapFromUrl(uri);
        }

        return bitmap;
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public Bitmap loadBitmapFromMemCache(String url) {
        final String key = MD5.digest(url);
        Bitmap bitmap = getBitmapFromMemCache(key);
        return bitmap;
    }

    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight)
            throws IOException {
        //显式禁止在UI线程进行I/O操作
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

        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
    }

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth,
            int reqHeight) throws IOException {
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
            FileInputStream fileInputStream = (FileInputStream)snapShot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor,
                    reqWidth, reqHeight);
            if (bitmap != null) {
                //将压缩后的图片放入内存缓存
                addBitmapToMemoryCache(key, bitmap);
            }
        }

        return bitmap;
    }


    public boolean downloadUrlToStream(String urlString,
            OutputStream outputStream) throws SocketTimeoutException {

        //连接超时的时间默认是10s，在这期间核心线程会被阻塞，新的任务会被添加到LinkedBrokingQueue中，
        //LinkedBrokingQueue默认长度是Integer.MAX_VALUE，所以新的任务会一直排队，导致即使想从磁盘中获取缓存
        //图片也还是会阻塞，所以将连接超时时间缩短为5s，并且对超时进行异常处理
        OkHttpClient client=new OkHttpClient.Builder()
                .connectTimeout(5,TimeUnit.SECONDS)   //SocketTimeOutException
                .build();
        Request request=new Request.Builder().url(urlString).build();
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            Response response=client.newCall(request).execute();
            in=new BufferedInputStream(response.body().byteStream(),IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;

        } catch (IOException e) {
            if(e instanceof SocketTimeoutException)
                throw (SocketTimeoutException)e;
            else
                e.printStackTrace();
        } finally {
            MyUtils.close(out);
            MyUtils.close(in);
        }
        return false;
    }

    public Bitmap downloadBitmapFromUrl(String urlString) throws SocketTimeoutException {
        OkHttpClient client=new OkHttpClient.Builder()
                .connectTimeout(5,TimeUnit.SECONDS)
                .build();
        Request request=new Request.Builder().url(urlString).build();
        BufferedInputStream in=null;
        Bitmap bitmap=null;
        try{
            Response response=client.newCall(request).execute();
            in = new BufferedInputStream(response.body().byteStream(), IO_BUFFER_SIZE);
            bitmap=BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            if(e instanceof SocketTimeoutException)
                throw (SocketTimeoutException)e;
            else
                e.printStackTrace();
        }finally {
            MyUtils.close(in);
        }

        return bitmap;
    }

    /**
        获取缓存目录，如果sd卡可用就用外部存储，否则用内部存储
        内部存储在机身内存不足时会删除缓存
     */
    public File getDiskCacheDir(Context context, String uniqueName) {
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
}
