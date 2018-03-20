package com.fanwe.lib.cache.api;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.handler.ICacheHandler;

/**
 * Created by zhengjun on 2018/3/20.
 */
public abstract class AbstractCache<T>
{
    private Class<T> mValueClass;
    private final ICacheHandler<T> mCacheHandler;

    public AbstractCache(IDiskInfo diskInfo)
    {
        mCacheHandler = onCreateCacheHandler(diskInfo);
    }

    public final void setValueClass(Class<T> clazz)
    {
        mValueClass = clazz;
    }

    public final Class<T> getValueClass()
    {
        if (mValueClass == null)
        {
            throw new NullPointerException("value class is null, you must invoke setValueClass(clazz) before this");
        }
        return mValueClass;
    }

    public final ICacheHandler<T> getCacheHandler()
    {
        return mCacheHandler;
    }

    protected abstract ICacheHandler<T> onCreateCacheHandler(IDiskInfo diskInfo);
}
