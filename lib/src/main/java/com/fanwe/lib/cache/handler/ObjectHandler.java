package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Object处理类
 */
public class ObjectHandler<T> extends StringConverterHandler<T>
{
    public ObjectHandler(DiskInfo diskInfo)
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

    private void checkObjectConverter()
    {
        if (getDiskInfo().getObjectConverter() == null)
        {
            throw new NullPointerException("you must provide an ObjectConverter instance before this");
        }
    }
}
