package com.fanwe.lib.cache.api;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.handler.ICacheHandler;

/**
 * Created by zhengjun on 2018/3/21.
 */
public abstract class AbstractObjectCache<T> implements IObjectCache<T>
{
    private final ICacheHandler<T> mHandler;
    private final Class<T> mClass;

    public AbstractObjectCache(IDiskInfo diskInfo, Class<T> clazz)
    {
        mHandler = onCreateCacheHandler(diskInfo);
        mClass = clazz;
    }

    protected abstract ICacheHandler<T> onCreateCacheHandler(IDiskInfo diskInfo);

    @Override
    public final boolean put(T value)
    {
        if (value == null)
        {
            return false;
        }

        final String key = value.getClass().getName();
        return mHandler.putCache(key, value);
    }

    @Override
    public final T get()
    {
        final String key = mClass.getName();
        return mHandler.getCache(key, mClass);
    }

    @Override
    public final boolean remove()
    {
        final String key = mClass.getName();
        return mHandler.removeCache(key);
    }
}
