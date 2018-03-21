package com.fanwe.lib.cache;

import com.fanwe.lib.cache.api.ICommonCache;
import com.fanwe.lib.cache.api.IObjectCache;
import com.fanwe.lib.cache.api.ISerializableCache;
import com.fanwe.lib.cache.api.impl.ObjectCache;
import com.fanwe.lib.cache.api.impl.SerializableCache;
import com.fanwe.lib.cache.converter.IEncryptConverter;
import com.fanwe.lib.cache.converter.IObjectConverter;
import com.fanwe.lib.cache.handler.impl.BooleanHandler;
import com.fanwe.lib.cache.handler.impl.DoubleHandler;
import com.fanwe.lib.cache.handler.impl.FloatHandler;
import com.fanwe.lib.cache.handler.impl.IntegerHandler;
import com.fanwe.lib.cache.handler.impl.LongHandler;
import com.fanwe.lib.cache.handler.impl.StringHandler;

import java.io.File;

public class FDisk extends FAbstractDisk
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

    //---------- Override start ----------

    @Override
    public FDisk setEncrypt(boolean encrypt)
    {
        super.setEncrypt(encrypt);
        return this;
    }

    @Override
    public FDisk setMemorySupport(boolean memorySupport)
    {
        super.setMemorySupport(memorySupport);
        return this;
    }

    @Override
    public FDisk setEncryptConverter(IEncryptConverter encryptConverter)
    {
        super.setEncryptConverter(encryptConverter);
        return this;
    }

    @Override
    public FDisk setObjectConverter(IObjectConverter objectConverter)
    {
        super.setObjectConverter(objectConverter);
        return this;
    }

    //---------- Override end ----------

    //---------- cache start ----------

    @Override
    public ICommonCache<Integer> cacheInteger()
    {
        return new IntegerHandler(this);
    }

    @Override
    public ICommonCache<Long> cacheLong()
    {
        return new LongHandler(this);
    }

    @Override
    public ICommonCache<Float> cacheFloat()
    {
        return new FloatHandler(this);
    }

    @Override
    public ICommonCache<Double> cacheDouble()
    {
        return new DoubleHandler(this);
    }

    @Override
    public ICommonCache<Boolean> cacheBoolean()
    {
        return new BooleanHandler(this);
    }

    @Override
    public ICommonCache<String> cacheString()
    {
        return new StringHandler(this);
    }

    @Override
    public ISerializableCache cacheSerializable()
    {
        return new SerializableCache(this);
    }

    @Override
    public IObjectCache cacheObject()
    {
        return new ObjectCache(this);
    }

    //---------- cache end ----------
}
