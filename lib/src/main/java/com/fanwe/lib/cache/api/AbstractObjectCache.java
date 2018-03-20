package com.fanwe.lib.cache.api;

import com.fanwe.lib.cache.IDiskInfo;

/**
 * Created by zhengjun on 2018/3/20.
 */
public abstract class AbstractObjectCache<T> extends AbstractCache<T> implements IObjectCache<T>
{
    public AbstractObjectCache(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    public final boolean put(T value)
    {
        final String key = getObjectClass().getName();
        return getCacheHandler().putCache(key, value);
    }

    @Override
    public final T get()
    {
        final String key = getObjectClass().getName();
        return getCacheHandler().getCache(key, getObjectClass());
    }

    @Override
    public final boolean remove()
    {
        final String key = getObjectClass().getName();
        return getCacheHandler().removeCache(key);
    }
}
