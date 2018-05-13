package com.example.imageloader.cache;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;

import com.example.imageloader.Utils;
import com.example.imageloader.resizer.DecodeOption;
import com.example.imageloader.resizer.ImageResizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DiskCache implements ImageCache {

    private static final String TAG = "DiskCache";

    private DiskLruCache mDiskLruCache;
    private ImageResizer mImageResizer;

    /**
     * 默认磁盘缓存参数：
     * 缓存大小50M
     * 节点数为1
     */
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int DISK_CACHE_INDEX = 0;
    private static final String DIRECTORY_NAME = "imageloader cache";

    //IO流用于缓冲区的数组大小为8k
    public static final int IO_BUFFER_SIZE = 8 * 1024;


    public DiskCache(Context context) {
        this(context, DISK_CACHE_SIZE);
    }

    public DiskCache(Context context, long size) {
        File diskCacheDir = getDiskCacheDir(context);
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        if (getUsableSpace(diskCacheDir) > size) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1,
                        size);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "ImageLoader: insufficient memory when create diskCache!");
        }
    }

    @Override
    public Bitmap get(String url, DecodeOption option) throws IOException {

        if (url == null) {
            Log.e(TAG, "ImageLoader: url can't be null");
            return null;
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "load bitmap from UI Thread, it's not recommended!");
        }

        if (isLocalResource(url)) {
            return loadInFile(url, option);
        } else {
            Bitmap bitmap = null;
            if (mDiskLruCache != null) {
                String key = Utils.digest(url);
                DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
                if (snapShot != null) {
                    FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(DISK_CACHE_INDEX);
                    FileDescriptor fileDescriptor = fileInputStream.getFD();
                    bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, option);
                    Log.d(TAG, "get: load bitmap success");
                } else {
                    Log.d(TAG, "get: url= " + url);
                    Log.d(TAG, "get: load bitmap fail");
                }
            }
            return bitmap;
        }
    }

    @Override
    public void put(String url, Bitmap bitmap, DecodeOption option) throws IOException {
        put(url, bitmap);
    }

    @Override
    public void put(String url, InputStream inputStream, DecodeOption option) throws IOException {
        put(url, inputStream);
    }

    public void put(String url, InputStream inputStream) throws IOException {
        if (mDiskLruCache != null && url != null && inputStream != null && !isLocalResource(url)) {
            DiskLruCache.Editor editor = mDiskLruCache.edit(Utils.digest(url));

            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                if (putFromInputStream(inputStream, outputStream)) {
                    editor.commit();
                    Log.d(TAG, "put: cache success");
                }else{
                    editor.abort();
                    Log.d(TAG, "put: cache fail");
                }
            } else {
                Log.d(TAG, "put: cache fail");
            }
        }
    }

    public void put(String url, Bitmap bitmap) throws IOException {

        if (mDiskLruCache != null && url != null && bitmap != null && !isLocalResource(url)) {
            DiskLruCache.Editor editor = mDiskLruCache.edit(Utils.digest(url));

            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                editor.commit();
                Log.d(TAG, "put: cache success");
            } else {
                Log.d(TAG, "put: cache fail");
            }
        }
    }

    private boolean putFromInputStream(InputStream inputStream,OutputStream outputStream) {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
            bos = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = bis.read()) != -1) {
                bos.write(b);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            Utils.close(bis);
            Utils.close(bos);
        }

        return false;
    }

    @Override
    public void setResizer(ImageResizer resizer) {
        mImageResizer = resizer;
    }

    /*
     * 获取缓存目录，如果sd卡可用就用外部存储，否则用内部存储
     * 内部存储在机身内存不足时会删除缓存
     */
    private File getDiskCacheDir(Context context) {
        boolean externalStorageAvailable = Environment
                .getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + DIRECTORY_NAME);
    }

    /*
     * 返回此抽象路径名指定的分区上可用于此虚拟机的字节数
     */
    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    private boolean isLocalResource(String uri) {
        return uri.startsWith("/");
    }

    //load in file and not cache
    private Bitmap loadInFile(String uri, DecodeOption option) {
        FileInputStream fis;
        FileDescriptor fd = null;
        try {
            fis = new FileInputStream(uri);
            fd = fis.getFD();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mImageResizer.decodeSampledBitmapFromFileDescriptor(fd, option);
    }
}
