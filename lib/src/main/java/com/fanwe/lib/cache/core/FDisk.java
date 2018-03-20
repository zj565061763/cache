package com.fanwe.lib.cache.core;

import com.fanwe.lib.cache.core.api.ICommonCache;
import com.fanwe.lib.cache.core.api.IObjectCache;
import com.fanwe.lib.cache.core.converter.IEncryptConverter;
import com.fanwe.lib.cache.core.converter.IObjectConverter;
import com.fanwe.lib.cache.core.handler.BooleanHandler;
import com.fanwe.lib.cache.core.handler.DoubleHandler;
import com.fanwe.lib.cache.core.handler.FloatHandler;
import com.fanwe.lib.cache.core.handler.IntegerHandler;
import com.fanwe.lib.cache.core.handler.LongHandler;
import com.fanwe.lib.cache.core.handler.ObjectHandler;
import com.fanwe.lib.cache.core.handler.SerializableHandler;
import com.fanwe.lib.cache.core.handler.StringHandler;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengjun on 2018/3/20.
 */
public class FDisk extends FAbstractDisk
{
    private static final String DEFAULT_FILE_DIR = "disk_file";
    private static final String DEFAULT_CACHE_DIR = "disk_cache";

    private static final Map<String, FDisk> MAP_INSTANCE = new HashMap<>();

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
    public synchronized static FDisk openDir(File directory)
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

        final String path = directory.getAbsolutePath();
        FDisk instance = MAP_INSTANCE.get(path);
        if (instance == null)
        {
            instance = new FDisk(directory);
            MAP_INSTANCE.put(path, instance);
        }
        return instance;
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

    private IntegerHandler mIntegerHandler;
    private LongHandler mLongHandler;
    private FloatHandler mFloatHandler;
    private DoubleHandler mDoubleHandler;
    private BooleanHandler mBooleanHandler;
    private StringHandler mStringHandler;

    private SerializableHandler mSerializableHandler;
    private ObjectHandler mObjectHandler;

    @Override
    public ICommonCache<Integer> cacheInteger()
    {
        if (mIntegerHandler == null)
        {
            mIntegerHandler = new IntegerHandler(this);
        }
        return mIntegerHandler;
    }

    @Override
    public ICommonCache<Long> cacheLong()
    {
        if (mLongHandler == null)
        {
            mLongHandler = new LongHandler(this);
        }
        return mLongHandler;
    }

    @Override
    public ICommonCache<Float> cacheFloat()
    {
        if (mFloatHandler == null)
        {
            mFloatHandler = new FloatHandler(this);
        }
        return mFloatHandler;
    }

    @Override
    public ICommonCache<Double> cacheDouble()
    {
        if (mDoubleHandler == null)
        {
            mDoubleHandler = new DoubleHandler(this);
        }
        return mDoubleHandler;
    }

    @Override
    public ICommonCache<Boolean> cacheBoolean()
    {
        if (mBooleanHandler == null)
        {
            mBooleanHandler = new BooleanHandler(this);
        }
        return mBooleanHandler;
    }

    @Override
    public ICommonCache<String> cacheString()
    {
        if (mStringHandler == null)
        {
            mStringHandler = new StringHandler(this);
        }
        return mStringHandler;
    }

    @Override
    public <T extends Serializable> IObjectCache<T> cacheSerializable()
    {
        if (mSerializableHandler == null)
        {
            mSerializableHandler = new SerializableHandler(this);
        }
        return mSerializableHandler;
    }

    @Override
    public <T> IObjectCache<T> cacheObject()
    {
        if (mObjectHandler == null)
        {
            mObjectHandler = new ObjectHandler(this);
        }
        return mObjectHandler;
    }

    //---------- cache end ----------
}
