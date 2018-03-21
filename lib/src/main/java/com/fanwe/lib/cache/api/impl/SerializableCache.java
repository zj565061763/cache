package com.fanwe.lib.cache.api.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.api.ISerializableCache;
import com.fanwe.lib.cache.handler.ICacheHandler;
import com.fanwe.lib.cache.handler.impl.SerializableHandler;

import java.io.Serializable;

/**
 * Created by zhengjun on 2018/3/20.
 */
public class SerializableCache implements ISerializableCache
{
    private final SerializableHandler mSerializableHandler;

    public SerializableCache(IDiskInfo diskInfo)
    {
        mSerializableHandler = new SerializableHandler(diskInfo);
    }

    @Override
    public boolean put(Serializable value)
    {
        if (value == null)
        {
            return false;
        }

        final String key = value.getClass().getName();
        return mSerializableHandler.putCache(key, value);
    }

    @Override
    public <T extends Serializable> T get(Class<T> clazz)
    {
        mSerializableHandler.getCache("", clazz);
        return null;
    }

    @Override
    public boolean remove()
    {
        return false;
    }
}
