package com.sd.lib.cache.handler;

import android.text.TextUtils;

import com.sd.lib.cache.Disk;
import com.sd.lib.cache.DiskInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存处理基类
 */
abstract class BaseCacheHandler<T> implements CacheHandler<T>, Disk.CommonCache<T>
{
    private final DiskInfo mDiskInfo;
    private static final Map<String, byte[]> MAP_MEMORY = new HashMap<>();
    private RealCacheHandler mRealCacheHandler;

    public BaseCacheHandler(DiskInfo diskInfo)
    {
        if (diskInfo == null)
            throw new NullPointerException("diskInfo is null");

        mDiskInfo = diskInfo;
    }

    protected final DiskInfo getDiskInfo()
    {
        return mDiskInfo;
    }

    protected abstract String getKeyPrefix();

    private String transformKey(final String key)
    {
        if (TextUtils.isEmpty(key))
            throw new IllegalArgumentException("key is null or empty");

        final String prefix = getKeyPrefix();
        if (TextUtils.isEmpty(prefix))
            throw new IllegalArgumentException("key prefix is null or empty");

        return prefix + key;
    }

    private RealCacheHandler getRealCacheHandler()
    {
        if (mRealCacheHandler == null)
            mRealCacheHandler = new FileCacheHandler();
        return mRealCacheHandler;
    }

    //---------- CacheHandler start ----------

    @Override
    public final boolean putCache(String key, T value)
    {
        synchronized (Disk.class)
        {
            if (value == null)
                return removeCache(key);

            key = transformKey(key);
            final byte[] data = transformValueToByte(value);
            if (data == null)
                throw new NullPointerException();

            final boolean result = getRealCacheHandler().putCache(key, data, getDiskInfo());

            if (result)
                putMemoryIfNeed(key, data);

            return result;
        }
    }

    @Override
    public final T getCache(String key, Class clazz)
    {
        synchronized (Disk.class)
        {
            key = transformKey(key);

            byte[] data = getMemory(key);
            if (data != null)
                return transformByteToValue(data, clazz);

            data = getRealCacheHandler().getCache(key, clazz, getDiskInfo());
            if (data != null)
            {
                putMemoryIfNeed(key, data);
                return transformByteToValue(data, clazz);
            }

            return null;
        }
    }

    @Override
    public final boolean removeCache(String key)
    {
        synchronized (Disk.class)
        {
            key = transformKey(key);

            removeMemory(key);
            return getRealCacheHandler().removeCache(key, getDiskInfo());
        }
    }

    //---------- CacheHandler end ----------


    //---------- CommonCache start ----------

    @Override
    public final boolean put(String key, T value)
    {
        return putCache(key, value);
    }

    @Override
    public final T get(String key)
    {
        return get(key, null);
    }

    @Override
    public final T get(String key, T defaultValue)
    {
        final T cache = getCache(key, null);
        return cache == null ? defaultValue : cache;
    }

    //---------- CommonCache end ----------


    //---------- memory start ----------

    private void putMemoryIfNeed(String key, byte[] value)
    {
        if (getDiskInfo().isMemorySupport())
            MAP_MEMORY.put(key, value);
    }

    private byte[] getMemory(String key)
    {
        return getDiskInfo().isMemorySupport() ? MAP_MEMORY.get(key) : null;
    }

    private void removeMemory(String key)
    {
        if (!MAP_MEMORY.isEmpty())
            MAP_MEMORY.remove(key);
    }

    //---------- memory end ----------

    private byte[] transformValueToByte(T value)
    {
        if (value == null)
            throw new NullPointerException();

        final boolean encrypt = getDiskInfo().isEncrypt();
        final Disk.EncryptConverter converter = getDiskInfo().getEncryptConverter();
        if (encrypt && converter == null)
            throw new RuntimeException("you must provide an EncryptConverter instance before this");

        byte[] data = valueToByte(value);
        if (data == null)
            throw new RuntimeException("valueToByte(T) method return null");

        if (encrypt)
        {
            data = converter.encrypt(data);
            if (data == null)
                throw new RuntimeException("EncryptConverter.encrypt(byte[]) method return null");
        }

        final byte[] dataWithTag = Arrays.copyOf(data, data.length + 1);
        dataWithTag[dataWithTag.length - 1] = (byte) (encrypt ? 1 : 0);

        return dataWithTag;
    }

    private T transformByteToValue(byte[] data, Class clazz)
    {
        if (data == null)
            throw new NullPointerException();

        final boolean isEncrypted = data[data.length - 1] == 1;
        final Disk.EncryptConverter converter = getDiskInfo().getEncryptConverter();
        if (isEncrypted && converter == null)
        {
            getDiskInfo().getExceptionHandler().onException(new RuntimeException("content is encrypted but EncryptConverter not found when try decrypt"));
            return null;
        }

        data = Arrays.copyOf(data, data.length - 1);

        if (isEncrypted)
        {
            data = converter.decrypt(data);
            if (data == null)
                return null;
        }

        try
        {
            return byteToValue(data, clazz);
        } catch (Exception e)
        {
            getDiskInfo().getExceptionHandler().onException(e);
            return null;
        }
    }

    /**
     * 缓存转byte
     *
     * @param value
     * @return
     */
    protected abstract byte[] valueToByte(T value);

    /**
     * byte转缓存
     *
     * @param bytes
     * @param clazz
     * @return
     */
    protected abstract T byteToValue(byte[] bytes, Class clazz);
}
