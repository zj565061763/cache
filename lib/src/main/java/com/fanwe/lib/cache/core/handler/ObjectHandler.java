package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;

/**
 * Object处理类
 */
public class ObjectHandler<T> extends StringConverterHandler<T>
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
}
