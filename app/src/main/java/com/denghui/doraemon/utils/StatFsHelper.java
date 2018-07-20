package com.denghui.doraemon.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.support.annotation.GuardedBy;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StatFsHelper {
    private static final StatFsHelper ourInstance = new StatFsHelper();

    public static StatFsHelper getInstance() {
        return ourInstance;
    }

    private StatFsHelper() {
        lock = new ReentrantLock();
    }

    public enum StorageType {
        INTERNAL,
        EXTERNAL
    };

    // Time interval for updating disk information
    private static final long RESTAT_INTERVAL_MS = TimeUnit.MINUTES.toMillis(2);

    private volatile StatFs mInternalStatFs = null;
    private volatile File mInternalPath;

    private volatile StatFs mExternalStatFs = null;
    private volatile File mExternalPath;

    @GuardedBy("lock")
    private long mLastRestatTime;

    private final Lock lock;
    private volatile boolean mInitialized = false;

    @SuppressLint("DeprecatedMethod")
    public long getFreeStorageSpace(StorageType storageType) {
        ensureInitialized();

        maybeUpdateStats();

        StatFs statFS = storageType == StorageType.INTERNAL ? mInternalStatFs : mExternalStatFs;
        if (statFS != null) {
            long blockSize, availableBlocks;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFS.getBlockSizeLong();
                availableBlocks = statFS.getFreeBlocksLong();
            } else {
                blockSize = statFS.getBlockSize();
                availableBlocks = statFS.getFreeBlocks();
            }
            return blockSize * availableBlocks;
        }
        return -1;
    }

    @SuppressLint("DeprecatedMethod")
    public long getTotalStorageSpace(StorageType storageType) {
        ensureInitialized();

        maybeUpdateStats();

        StatFs statFS = storageType == StorageType.INTERNAL ? mInternalStatFs : mExternalStatFs;
        if (statFS != null) {
            long blockSize, totalBlocks;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFS.getBlockSizeLong();
                totalBlocks = statFS.getBlockCountLong();
            } else {
                blockSize = statFS.getBlockSize();
                totalBlocks = statFS.getBlockCount();
            }
            return blockSize * totalBlocks;
        }
        return -1;
    }

    @SuppressLint("DeprecatedMethod")
    public long getAvailableStorageSpace(StorageType storageType) {
        ensureInitialized();

        maybeUpdateStats();

        StatFs statFS = storageType == StorageType.INTERNAL ? mInternalStatFs : mExternalStatFs;
        if (statFS != null) {
            long blockSize, availableBlocks;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFS.getBlockSizeLong();
                availableBlocks = statFS.getAvailableBlocksLong();
            } else {
                blockSize = statFS.getBlockSize();
                availableBlocks = statFS.getAvailableBlocks();
            }
            return blockSize * availableBlocks;
        }
        return 0;
    }

    private void ensureInitialized() {
        if (!mInitialized){
            lock.lock();
            try {
                mInternalPath = Environment.getDataDirectory();
                mExternalPath = Environment.getExternalStorageDirectory();
                updateStats();
                mInitialized = true;
            } finally {
                lock.unlock();
            }
        }
    }

    private void maybeUpdateStats() {
        if (lock.tryLock()) {
            try {
                if (SystemClock.uptimeMillis() - mLastRestatTime > RESTAT_INTERVAL_MS) {
                    updateStats();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void updateStats() {
        mInternalStatFs = updateStatsHelper(mInternalStatFs, mInternalPath);
        mExternalStatFs = updateStatsHelper(mExternalStatFs, mExternalPath);
        mLastRestatTime = SystemClock.uptimeMillis();
    }

    private StatFs updateStatsHelper(StatFs statfs, File dir) {
        if(dir == null || !dir.exists()) {
            // The path does not exist, do not track stats for it.
            return null;
        }

        try {
            if (statfs == null) {
                // Create a new StatFs object for this path.
                statfs = createStatFs(dir.getAbsolutePath());
            } else {
                // Call restat and keep the existing StatFs object.
                statfs.restat(dir.getAbsolutePath());
            }
        } catch (IllegalArgumentException ex) {
            // Invalidate the StatFs object for this directory. The native StatFs implementation throws
            // IllegalArgumentException in the case that the statfs() system call fails and it invalidates
            // its internal data structures so subsequent calls against the StatFs object will fail or
            // throw (so we should make no more calls on the object). The most likely reason for this call
            // to fail is because the provided path no longer exists. The next call to updateStats() will
            // a new statfs object if the path exists. This will handle the case that a path is unmounted
            // and later remounted (but it has to have been mounted when this object was initialized).
            statfs = null;
        } catch (Throwable ex) {
            // Any other exception types are not expected and should be propagated as runtime errors.


// TODO: 2018/7/20 没有引入Throwable类
//            throw Throwables.propagate(ex);
        }

        return statfs;
    }

    protected static StatFs createStatFs(String path) {
        return new StatFs(path);
    }


}
