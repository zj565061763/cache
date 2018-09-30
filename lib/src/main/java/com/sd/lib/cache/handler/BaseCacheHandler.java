package com.sd.lib.cache.handler;

import android.text.TextUtils;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;

import java.util.Arrays;

/**
 * 缓存处理基类
 */
abstract class BaseCacheHandler<T> implements CacheHandler<T>, Cache.CommonCache<T>
{
    private final CacheInfo mCacheInfo;

    public BaseCacheHandler(CacheInfo cacheInfo)
    {
        if (cacheInfo == null)
            throw new NullPointerException();

        mCacheInfo = cacheInfo;
    }

    protected final CacheInfo getCacheInfo()
    {
        return mCacheInfo;
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

    private Cache.CacheStore getCacheStore()
    {
        return getCacheInfo().getCacheStore();
    }

    //---------- CacheHandler start ----------

    @Override
    public final boolean putCache(String key, T value)
    {
        synchronized (Cache.class)
        {
            if (value == null)
                return removeCache(key);

            key = transformKey(key);
            final byte[] data = transformValueToByte(value);
            if (data == null)
                throw new NullPointerException();

            return getCacheStore().putCache(key, data, getCacheInfo());
        }
    }

    @Override
    public final T getCache(String key, Class clazz)
    {
        synchronized (Cache.class)
        {
            key = transformKey(key);

            byte[] data = getCacheStore().getCache(key, clazz, getCacheInfo());
            if (data == null)
                return null;

            return transformByteToValue(data, clazz);
        }
    }

    @Override
    public final boolean removeCache(String key)
    {
        synchronized (Cache.class)
        {
            key = transformKey(key);

            return getCacheStore().removeCache(key, getCacheInfo());
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

    private byte[] transformValueToByte(T value)
    {
        if (value == null)
            throw new NullPointerException();

        final boolean encrypt = getCacheInfo().isEncrypt();
        final Cache.EncryptConverter converter = getCacheInfo().getEncryptConverter();
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
        final Cache.EncryptConverter converter = getCacheInfo().getEncryptConverter();
        if (isEncrypted && converter == null)
        {
            getCacheInfo().getExceptionHandler().onException(new RuntimeException("data is encrypted but EncryptConverter not found when try decrypt"));
            return null;
        }

        data = Arrays.copyOf(data, data.length - 1);

        if (isEncrypted)
        {
            data = converter.decrypt(data);
            if (data == null)
            {
                getCacheInfo().getExceptionHandler().onException(new RuntimeException("data is encrypted but EncryptConverter return null"));
                return null;
            }
        }

        try
        {
            return byteToValue(data, clazz);
        } catch (Exception e)
        {
            getCacheInfo().getExceptionHandler().onException(e);
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
