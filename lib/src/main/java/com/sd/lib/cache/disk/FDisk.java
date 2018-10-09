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

    //---------- open start ----------

    /**
     * 内部存储目录"/data/包名/files/disk_file"
     *
     * @return
     */
    public static final DiskCache open()
    {
        return new FDisk(getInternalFilesDir(DEFAULT_FILE_DIR));
    }

    /**
     * 优先使用外部存储，如果外部存储不存在，则使用内部存储
     * <p>
     * 外部存储目录"Android/data/包名/files/disk_file"
     *
     * @return
     */
    public static final DiskCache openExternal()
    {
        File directory = getExternalFilesDir(DEFAULT_FILE_DIR);
        if (!checkDirectory(directory))
            directory = getInternalFilesDir(DEFAULT_FILE_DIR);

        return new FDisk(directory);
    }

    /**
     * 使用指定的目录
     *
     * @param directory
     * @return
     */
    public static final DiskCache openDir(File directory)
    {
        return new FDisk(directory);
    }

    //---------- open end ----------

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

    /**
     * 返回外部存储"Android/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getExternalFilesDir(String dirName)
    {
        return getCacheConfig().mContext.getExternalFilesDir(dirName);
    }

    /**
     * 返回内部存储"/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getInternalFilesDir(String dirName)
    {
        return new File(getCacheConfig().mContext.getFilesDir(), dirName);
    }

    /**
     * 检查目录是否可用
     *
     * @param directory
     * @return
     */
    protected static final boolean checkDirectory(File directory)
    {
        if (directory.exists())
            return true;

        synchronized (Cache.class)
        {
            if (directory.exists())
                return true;

            return directory.mkdirs();
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
