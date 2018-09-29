package com.sd.lib.cache.handler;

import android.text.TextUtils;

import com.sd.lib.cache.Disk;
import com.sd.lib.cache.DiskInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存处理基类
 */
abstract class BaseHandler<T> implements CacheHandler<T>, Disk.CommonCache<T>
{
    private final DiskInfo mDiskInfo;
    private static final Map<String, Object> MAP_MEMORY = new HashMap<>();

    public BaseHandler(DiskInfo diskInfo)
    {
        if (diskInfo == null)
            throw new NullPointerException("diskInfo is null");

        mDiskInfo = diskInfo;
    }

    protected final DiskInfo getDiskInfo()
    {
        return mDiskInfo;
    }

    private final String getRealKey(final String key, boolean encrypt)
    {
        if (TextUtils.isEmpty(key))
            throw new IllegalArgumentException("key is null or empty");

        final String prefix = getKeyPrefix();
        if (TextUtils.isEmpty(prefix))
            throw new IllegalArgumentException("key prefix is null or empty");

        if (encrypt)
            return prefix + Utils.MD5(key);
        else
            return prefix + key;
    }

    private File getCacheFile(String key)
    {
        final String realKey = getRealKey(key, true);
        final File dir = getDiskInfo().getDirectory();
        return new File(dir, realKey);
    }

    //---------- CacheHandler start ----------

    @Override
    public final boolean putCache(String key, T value)
    {
        synchronized (Disk.class)
        {
            if (value == null)
            {
                removeCache(key);
                return true;
            }

            final File file = getCacheFile(key);
            final boolean result = putCacheImpl(key, value, file);

            if (result)
                putMemoryIfNeed(key, value);

            return result;
        }
    }

    @Override
    public final T getCache(String key, Class clazz)
    {
        synchronized (Disk.class)
        {
            final Object result = getMemory(key);
            if (result != null)
                return (T) result;

            final File file = getCacheFile(key);
            if (!file.exists())
                return null;

            final T cache = getCacheImpl(key, clazz, file);
            if (cache != null)
                putMemoryIfNeed(key, cache);

            return cache;
        }
    }

    @Override
    public final boolean removeCache(String key)
    {
        synchronized (Disk.class)
        {
            removeMemory(key);

            final File file = getCacheFile(key);
            if (file.exists())
                return file.delete();
            else
                return true;
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

    private void putMemoryIfNeed(String key, Object value)
    {
        if (getDiskInfo().isMemorySupport())
        {
            final String realKey = getRealKey(key, false);
            MAP_MEMORY.put(realKey, value);
        }
    }

    private Object getMemory(String key)
    {
        if (getDiskInfo().isMemorySupport())
        {
            final String realKey = getRealKey(key, false);
            return MAP_MEMORY.get(realKey);
        }
        return null;
    }

    private void removeMemory(String key)
    {
        if (MAP_MEMORY.isEmpty())
            return;

        final String realKey = getRealKey(key, false);
        MAP_MEMORY.remove(realKey);
    }

    //---------- memory end ----------

    protected abstract String getKeyPrefix();

    protected abstract boolean putCacheImpl(String key, T value, File file);

    protected abstract T getCacheImpl(String key, Class clazz, File file);
}
