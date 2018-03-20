package com.fanwe.lib.cache.api.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.api.AbstractObjectCache;
import com.fanwe.lib.cache.handler.ICacheHandler;
import com.fanwe.lib.cache.handler.impl.ObjectHandler;

/**
 * Created by zhengjun on 2018/3/20.
 */
public class ObjectCache<T> extends AbstractObjectCache<T>
{
    public ObjectCache(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected ICacheHandler<T> onCreateCacheHandler(IDiskInfo diskInfo)
    {
        return new ObjectHandler<>(diskInfo);
    }
}
