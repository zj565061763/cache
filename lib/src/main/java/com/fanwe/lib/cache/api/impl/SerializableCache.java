package com.fanwe.lib.cache.api.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.api.ISerializableCache;
import com.fanwe.lib.cache.handler.impl.SerializableHandler;

import java.io.Serializable;

/**
 * 序列化缓存
 */
public class SerializableCache implements ISerializableCache
{
    private final IDiskInfo mDiskInfo;

    public SerializableCache(IDiskInfo diskInfo)
    {
        mDiskInfo = diskInfo;
    }

    @Override
    public <T extends Serializable> boolean put(T value)
    {
        if (value == null)
        {
            return false;
        }

        final String key = value.getClass().getName();
        return new SerializableHandler<T>(mDiskInfo).putCache(key, value);
    }

    @Override
    public <T extends Serializable> T get(Class<T> clazz)
    {
        final String key = clazz.getName();
        return new SerializableHandler<T>(mDiskInfo).getCache(key, clazz);
    }

    @Override
    public <T extends Serializable> boolean remove(Class<T> clazz)
    {
        final String key = clazz.getName();
        return new SerializableHandler<T>(mDiskInfo).removeCache(key);
    }
}
