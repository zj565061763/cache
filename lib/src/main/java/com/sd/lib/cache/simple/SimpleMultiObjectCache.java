package com.sd.lib.cache.simple;

import android.text.TextUtils;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;
import com.sd.lib.cache.handler.ObjectHandler;

public class SimpleMultiObjectCache<T> implements Cache.MultiObjectCache<T>
{
    private final ObjectHandler mObjectHandler;
    public final Class<T> mObjectClass;

    public SimpleMultiObjectCache(CacheInfo info, Class<T> objectClass)
    {
        if (objectClass == null)
            throw new NullPointerException("objectClass is null");

        mObjectClass = objectClass;
        mObjectHandler = new ObjectHandler(info)
        {
            @Override
            protected String getKeyPrefix()
            {
                return "multi_object_";
            }
        };
    }

    @Override
    public boolean put(String key, T value)
    {
        if (TextUtils.isEmpty(key))
            throw new IllegalArgumentException("key is null or empty");

        key += mObjectClass.getName();
        return mObjectHandler.putCache(key, value);
    }

    @Override
    public T get(String key)
    {
        if (TextUtils.isEmpty(key))
            throw new IllegalArgumentException("key is null or empty");

        key += mObjectClass.getName();
        return (T) mObjectHandler.getCache(key, mObjectClass);
    }

    @Override
    public boolean remove(String key)
    {
        if (TextUtils.isEmpty(key))
            throw new IllegalArgumentException("key is null or empty");

        key += mObjectClass.getName();
        return mObjectHandler.removeCache(key);
    }

    @Override
    public boolean contains(String key)
    {
        if (TextUtils.isEmpty(key))
            throw new IllegalArgumentException("key is null or empty");

        key += mObjectClass.getName();
        return mObjectHandler.containsCache(key);
    }
}
