package com.fanwe.lib.cache.api.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.api.AbstractObjectCache;
import com.fanwe.lib.cache.handler.ICacheHandler;
import com.fanwe.lib.cache.handler.impl.SerializableHandler;

import java.io.Serializable;

/**
 * 序列化缓存处理类
 */
public class SerializableCache<T extends Serializable> extends AbstractObjectCache<T>
{
    public SerializableCache(IDiskInfo diskInfo, Class<T> clazz)
    {
        super(diskInfo, clazz);
    }

    @Override
    protected ICacheHandler<T> onCreateCacheHandler(IDiskInfo diskInfo)
    {
        return new SerializableHandler<>(diskInfo);
    }
}
