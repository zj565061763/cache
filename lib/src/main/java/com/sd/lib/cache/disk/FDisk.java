package com.sd.lib.cache.disk;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.FCache;
import com.sd.lib.cache.store.SimpleDiskCacheStore;

import java.io.File;

public class FDisk extends FCache implements DiskCache
{
    private static final String DEFAULT_FILE_DIR = "disk_file";

    private final File mDirectory;
    private final CacheStore mCacheStore;

    protected FDisk(File directory)
    {
        if (directory == null)
            throw new NullPointerException();

        mDirectory = directory;
        mCacheStore = new SimpleDiskCacheStore(directory);
    }

    /**
     * 使用内部存储目录"/data/包名/files/disk_file"
     *
     * @return
     */
    public static final DiskCache open()
    {
        final File directory = new File(getCacheConfig().mContext.getFilesDir(), DEFAULT_FILE_DIR);
        return new FDisk(directory);
    }

    @Override
    public CacheStore getCacheStore()
    {
        return mCacheStore;
    }

    @Override
    public long size()
    {
        synchronized (Cache.class)
        {
            return getFileOrDirSize(mDirectory);
        }
    }

    @Override
    public void delete()
    {
        synchronized (Cache.class)
        {
            deleteFileOrDir(mDirectory);
        }
    }

    private static boolean deleteFileOrDir(File path)
    {
        if (path == null || !path.exists())
            return true;

        if (path.isFile())
            return path.delete();

        final File[] files = path.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                deleteFileOrDir(file);
            }
        }
        return path.delete();
    }

    private static long getFileOrDirSize(File file)
    {
        if (file == null || !file.exists())
            return 0;

        if (!file.isDirectory())
            return file.length();

        final File[] files = file.listFiles();
        if (files == null || files.length <= 0)
            return 0;

        long length = 0;
        for (File item : files)
        {
            length += getFileOrDirSize(item);
        }
        return length;
    }
}
