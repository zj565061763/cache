package com.sd.lib.cache.handler;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;

public class MultiObjectHandler extends ObjectHandler implements Cache.MultiObjectCache
{
    public MultiObjectHandler(CacheInfo info)
    {
        super(info);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "multi_object_";
    }

    @Override
    public <T> T get(String key, Class<T> clazz)
    {
        return (T) getCache(key, clazz);
    }
}
