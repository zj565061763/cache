package com.fanwe.lib.cache.api.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.api.AbstractObjectCache;
import com.fanwe.lib.cache.handler.ICacheHandler;
import com.fanwe.lib.cache.handler.impl.SerializableHandler;

import java.io.Serializable;

/**
 * Created by zhengjun on 2018/3/20.
 */
public class SerializableCache<T extends Serializable> extends AbstractObjectCache<T>
{
    public SerializableCache(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected ICacheHandler<T> onCreateCacheHandler(IDiskInfo diskInfo)
    {
        return new SerializableHandler<>(diskInfo);
    }
}
