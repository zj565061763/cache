package com.fanwe.lib.cache.api;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.handler.ICacheHandler;

/**
 * Created by zhengjun on 2018/3/20.
 */
public abstract class AbstractCache<T>
{
    private final ICacheHandler<T> mCacheHandler;

    public AbstractCache(IDiskInfo diskInfo)
    {
        mCacheHandler = onCreateCacheHandler(diskInfo);
    }

    public final ICacheHandler<T> getCacheHandler()
    {
        return mCacheHandler;
    }

    protected abstract ICacheHandler<T> onCreateCacheHandler(IDiskInfo diskInfo);
}
