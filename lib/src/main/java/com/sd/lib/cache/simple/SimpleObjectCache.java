package com.sd.lib.cache.simple;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;
import com.sd.lib.cache.handler.ObjectHandler;

public class SimpleObjectCache implements Cache.ObjectCache
{
    private final ObjectHandler mObjectHandler;

    public SimpleObjectCache(CacheInfo info)
    {
        mObjectHandler = new ObjectHandler(info)
        {
            @Override
            protected String getKeyPrefix()
            {
                return "object_";
            }
        };
    }

    @Override
    public boolean put(Object value)
    {
        final String key = value.getClass().getName();
        return mObjectHandler.putCache(key, value);
    }

    @Override
    public <T> T get(Class<T> clazz)
    {
        final String key = clazz.getName();
        return (T) mObjectHandler.getCache(key, clazz);
    }

    @Override
    public boolean remove(Class<?> clazz)
    {
        final String key = clazz.getName();
        return mObjectHandler.removeCache(key);
    }

    @Override
    public boolean contains(Class<?> clazz)
    {
        final String key = clazz.getName();
        return mObjectHandler.containsCache(key);
    }
}
