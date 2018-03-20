package com.fanwe.lib.cache.api;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.handler.ICacheHandler;

/**
 * Created by zhengjun on 2018/3/20.
 */
public abstract class AbstractCache<T>
{
    private Class<T> mObjectClass;
    private final ICacheHandler<T> mCacheHandler;

    public AbstractCache(IDiskInfo diskInfo)
    {
        mCacheHandler = onCreateCacheHandler(diskInfo);
    }

    public final void setObjectClass(Class<T> clazz)
    {
        mObjectClass = clazz;
    }

    public final Class<T> getObjectClass()
    {
        if (mObjectClass == null)
        {
            throw new NullPointerException("mObjectClass is null, you must invoke setObjectClass(clazz) before this");
        }
        return mObjectClass;
    }

    public final ICacheHandler<T> getCacheHandler()
    {
        return mCacheHandler;
    }

    protected abstract ICacheHandler<T> onCreateCacheHandler(IDiskInfo diskInfo);
}
