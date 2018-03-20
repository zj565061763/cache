package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;
import com.fanwe.lib.cache.core.api.IObjectCache;

/**
 * Object处理类
 */
public class ObjectHandler<T> extends StringConverterHandler<T> implements IObjectCache<T>
{
    public ObjectHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(T value)
    {
        checkObjectConverter();
        return getDiskInfo().getObjectConverter().objectToString(value);
    }

    @Override
    protected T stringToValue(String string, Class<T> clazz)
    {
        checkObjectConverter();
        return getDiskInfo().getObjectConverter().stringToObject(string, clazz);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "object_";
    }

    @Override
    public boolean put(T value)
    {
        if (value == null)
        {
            return false;
        }

        final String key = value.getClass().getName();
        return putCache(key, value);
    }

    @Override
    public T get(Class<T> clazz)
    {
        final String key = clazz.getName();
        return getCache(key, clazz);
    }

    @Override
    public boolean remove(Class<T> clazz)
    {
        final String key = clazz.getName();
        return removeCache(key);
    }
}
