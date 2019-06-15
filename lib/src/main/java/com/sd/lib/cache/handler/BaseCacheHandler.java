package com.sd.lib.cache.handler;

import android.text.TextUtils;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存处理基类
 */
abstract class BaseCacheHandler<T> implements CacheHandler<T>, Cache.CommonCache<T>
{
    private final CacheInfo mCacheInfo;
    private static final Map<String, byte[]> MAP_MEMORY = new HashMap<>();

    public BaseCacheHandler(CacheInfo cacheInfo)
    {
        if (cacheInfo == null)
            throw new IllegalArgumentException("cacheInfo is null when create: " + getClass().getName());
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

            final byte[] data = transformValueToByte(key, value);
            if (data == null)
                throw new NullPointerException("transformValueToByte return null when putCache: " + key);

            final boolean result = getCacheStore().putCache(key, data, getCacheInfo());
            if (result)
                putMemory(key, data);

            return result;
        }
    }

    @Override
    public final T getCache(String key, Class clazz)
    {
        synchronized (Cache.class)
        {
            key = transformKey(key);

            byte[] data = getMemory(key);
            if (data == null)
                data = getCacheStore().getCache(key, clazz, getCacheInfo());

            if (data == null)
                return null;

            return transformByteToValue(key, data, clazz);
        }
    }

    @Override
    public final boolean removeCache(String key)
    {
        synchronized (Cache.class)
        {
            key = transformKey(key);

            removeMemory(key);
            return getCacheStore().removeCache(key, getCacheInfo());
        }
    }

    @Override
    public final boolean containsCache(String key)
    {
        synchronized (Cache.class)
        {
            key = transformKey(key);

            if (getMemory(key) != null)
                return true;

            return getCacheStore().containsCache(key, getCacheInfo());
        }
    }

    //---------- CacheHandler end ----------

    //---------- memory start ----------

    private void putMemory(String key, byte[] value)
    {
        if (getCacheInfo().isMemorySupport())
            MAP_MEMORY.put(key, value);
    }

    private byte[] getMemory(String key)
    {
        return getCacheInfo().isMemorySupport() ? MAP_MEMORY.get(key) : null;
    }

    private void removeMemory(String key)
    {
        if (!MAP_MEMORY.isEmpty())
            MAP_MEMORY.remove(key);
    }

    //---------- memory end ----------


    //---------- CommonCache start ----------

    @Override
    public final boolean put(String key, T value)
    {
        return putCache(key, value);
    }

    @Override
    public final T get(String key, T defaultValue)
    {
        final T cache = getCache(key, null);
        return cache == null ? defaultValue : cache;
    }

    @Override
    public final boolean remove(String key)
    {
        return removeCache(key);
    }

    @Override
    public final boolean contains(String key)
    {
        return containsCache(key);
    }

    //---------- CommonCache end ----------

    private byte[] transformValueToByte(String key, T value)
    {
        if (value == null)
            throw new IllegalArgumentException("value is null when invoke transformValueToByte()");

        final boolean encrypt = getCacheInfo().isEncrypt();
        final Cache.EncryptConverter converter = getCacheInfo().getEncryptConverter();
        if (encrypt && converter == null)
            throw new RuntimeException("Encrypt is required but EncryptConverter is null. key:" + key);

        byte[] data = valueToByte(value);
        if (data == null)
            throw new RuntimeException("valueToByte(T) method return null. key:" + key);

        if (encrypt)
        {
            data = converter.encrypt(data);
            if (data == null)
                throw new RuntimeException("EncryptConverter.encrypt return null. key:" + key);
        }

        final byte[] dataWithTag = Arrays.copyOf(data, data.length + 1);
        dataWithTag[dataWithTag.length - 1] = (byte) (encrypt ? 1 : 0);

        return dataWithTag;
    }

    private T transformByteToValue(String key, byte[] data, Class clazz)
    {
        if (data == null)
            throw new IllegalArgumentException("data is null when invoke transformByteToValue()");

        if (data.length <= 0)
            return null;

        final boolean isEncrypted = data[data.length - 1] == 1;
        final Cache.EncryptConverter converter = getCacheInfo().getEncryptConverter();
        if (isEncrypted && converter == null)
        {
            getCacheInfo().getExceptionHandler().onException(new RuntimeException("Data is encrypted but EncryptConverter not found while try decrypt. key:" + key));
            return null;
        }

        data = Arrays.copyOf(data, data.length - 1);

        if (isEncrypted)
        {
            data = converter.decrypt(data);
            if (data == null)
            {
                getCacheInfo().getExceptionHandler().onException(new RuntimeException("EncryptConverter.decrypt return null. key:" + key));
                return null;
            }
        }

        return byteToValue(data, clazz);
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
