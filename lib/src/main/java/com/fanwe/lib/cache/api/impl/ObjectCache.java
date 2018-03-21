package com.fanwe.lib.cache.api.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.api.AbstractObjectCache;
import com.fanwe.lib.cache.handler.ICacheHandler;
import com.fanwe.lib.cache.handler.impl.ObjectHandler;

/**
 * 对象缓存处理类
 */
public class ObjectCache<T> extends AbstractObjectCache<T>
{
    public ObjectCache(IDiskInfo diskInfo, Class<T> clazz)
    {
        super(diskInfo, clazz);
    }

    @Override
    protected ICacheHandler<T> onCreateCacheHandler(IDiskInfo diskInfo)
    {
        return new ObjectHandler<>(diskInfo);
    }
}
