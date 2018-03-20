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
        return getCacheHandler().putCache(getValueClass().getName(), value);
    }

    @Override
    public final T get()
    {
        return getCacheHandler().getCache(getValueClass().getName(), getValueClass());
    }

    @Override
    public final boolean remove()
    {
        return getCacheHandler().removeCache(getValueClass().getName());
    }
}
