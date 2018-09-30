package com.sd.lib.cache.store;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;

import java.io.File;

/**
 * 内存和文件两级缓存
 */
public class SimpleMemoryDiskCacheStore implements Cache.CacheStore
{
    private final Cache.CacheStore mDisk;
    private Cache.CacheStore mMemory;

    private boolean mMemorySupport;

    public SimpleMemoryDiskCacheStore(File directory)
    {
        mDisk = new SimpleDiskCacheStore(directory);
    }

    /**
     * 设置是否打开内存存储功能
     *
     * @param support
     */
    public void setMemorySupport(boolean support)
    {
        mMemorySupport = support;
        if (!support)
            mMemory = null;
    }

    private Cache.CacheStore getMemory()
    {
        if (mMemory == null)
            mMemory = new SimpleMemoryCacheStore();
        return mMemory;
    }

    @Override
    public boolean putCache(String key, byte[] value, CacheInfo info)
    {
        final boolean result = mDisk.putCache(key, value, info);
        final boolean support = mMemorySupport;

        if (support && result)
            getMemory().putCache(key, value, info);

        return result;
    }

    @Override
    public byte[] getCache(String key, Class clazz, CacheInfo info)
    {
        byte[] result = null;

        final boolean support = mMemorySupport;
        if (support)
        {
            result = getMemory().getCache(key, clazz, info);
            if (result != null)
                return result;
        }

        result = mDisk.getCache(key, clazz, info);
        if (support && result != null)
            getMemory().putCache(key, result, info);

        return result;
    }

    @Override
    public boolean removeCache(String key, CacheInfo info)
    {
        getMemory().removeCache(key, info);
        return mDisk.removeCache(key, info);
    }
}
