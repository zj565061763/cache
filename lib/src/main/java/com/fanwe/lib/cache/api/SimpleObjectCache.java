package com.fanwe.lib.cache.api;

import com.fanwe.lib.cache.DiskInfo;
import com.fanwe.lib.cache.handler.ObjectHandler;

/**
 * 对象缓存
 */
public class SimpleObjectCache implements ObjectCache
{
    private final DiskInfo mDiskInfo;

    public SimpleObjectCache(DiskInfo diskInfo)
    {
        mDiskInfo = diskInfo;
    }

    @Override
    public boolean put(Object value)
    {
        if (value == null)
            return false;

        final String key = value.getClass().getName();
        return new ObjectHandler<>(mDiskInfo).putCache(key, value);
    }

    @Override
    public <T> T get(Class<T> clazz)
    {
        final String key = clazz.getName();
        return new ObjectHandler<T>(mDiskInfo).getCache(key, clazz);
    }

    @Override
    public boolean remove(Class clazz)
    {
        final String key = clazz.getName();
        return new ObjectHandler<>(mDiskInfo).removeCache(key);
    }
}
