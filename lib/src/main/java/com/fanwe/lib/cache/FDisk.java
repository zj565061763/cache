package com.fanwe.lib.cache;

import com.fanwe.lib.cache.api.CommonCache;
import com.fanwe.lib.cache.api.ObjectCache;
import com.fanwe.lib.cache.api.SerializableCache;
import com.fanwe.lib.cache.handler.BooleanHandler;
import com.fanwe.lib.cache.handler.DoubleHandler;
import com.fanwe.lib.cache.handler.FloatHandler;
import com.fanwe.lib.cache.handler.IntegerHandler;
import com.fanwe.lib.cache.handler.LongHandler;
import com.fanwe.lib.cache.handler.StringHandler;

import java.io.File;

public class FDisk extends BaseDisk<FDisk>
{
    private static final String DEFAULT_FILE_DIR = "disk_file";
    private static final String DEFAULT_CACHE_DIR = "disk_cache";

    protected FDisk(File directory)
    {
        super(directory);
    }

    //---------- open start ----------

    /**
     * 外部存储"Android/data/包名/files/disk_file"目录
     *
     * @return
     */
    public static FDisk open()
    {
        return openDir(getExternalFilesDir(DEFAULT_FILE_DIR));
    }

    /**
     * 外部存储"Android/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static FDisk open(String dirName)
    {
        return openDir(getExternalFilesDir(dirName));
    }

    /**
     * 外部存储"Android/data/包名/cache/disk_cache"目录
     *
     * @return
     */
    public static FDisk openCache()
    {
        return openDir(getExternalCacheDir(DEFAULT_CACHE_DIR));
    }

    /**
     * 外部存储"Android/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static FDisk openCache(String dirName)
    {
        return openDir(getExternalCacheDir(dirName));
    }

    /**
     * 内部存储"/data/包名/files/disk_file"目录
     *
     * @return
     */
    public static FDisk openInternal()
    {
        return openDir(getInternalFilesDir(DEFAULT_FILE_DIR));
    }

    /**
     * 内部存储"/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static FDisk openInternal(String dirName)
    {
        return openDir(getInternalFilesDir(dirName));
    }

    /**
     * 内部存储"/data/包名/cache/disk_cache"目录
     *
     * @return
     */
    public static FDisk openInternalCache()
    {
        return openDir(getInternalCacheDir(DEFAULT_CACHE_DIR));
    }

    /**
     * 内部存储"/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static FDisk openInternalCache(String dirName)
    {
        return openDir(getInternalCacheDir(dirName));
    }

    /**
     * 打开指定的目录
     *
     * @param directory
     * @return
     */
    public static FDisk openDir(File directory)
    {
        if (directory == null)
        {
            throw new NullPointerException("directory must not be null");
        } else
        {
            if (!directory.exists() && !directory.mkdirs())
            {
                throw new IllegalArgumentException("directory can not be create, theck the directory path");
            }
        }

        return new FDisk(directory);
    }

    //---------- open end ----------

    //---------- cache start ----------

    @Override
    public CommonCache<Integer> cacheInteger()
    {
        return new IntegerHandler(this);
    }

    @Override
    public CommonCache<Long> cacheLong()
    {
        return new LongHandler(this);
    }

    @Override
    public CommonCache<Float> cacheFloat()
    {
        return new FloatHandler(this);
    }

    @Override
    public CommonCache<Double> cacheDouble()
    {
        return new DoubleHandler(this);
    }

    @Override
    public CommonCache<Boolean> cacheBoolean()
    {
        return new BooleanHandler(this);
    }

    @Override
    public CommonCache<String> cacheString()
    {
        return new StringHandler(this);
    }

    @Override
    public SerializableCache cacheSerializable()
    {
        return new com.fanwe.lib.cache.api.impl.SerializableCache(this);
    }

    @Override
    public ObjectCache cacheObject()
    {
        return new com.fanwe.lib.cache.api.impl.ObjectCache(this);
    }

    //---------- cache end ----------
}
