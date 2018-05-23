package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Object处理类
 */
public class ObjectHandler<T> extends ByteConverterHandler<T>
{
    public ObjectHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(T value)
    {
        checkObjectConverter();
        return getDiskInfo().getObjectConverter().objectToByte(value);
    }

    @Override
    protected T byteToValue(byte[] bytes, Class<T> clazz)
    {
        checkObjectConverter();
        return getDiskInfo().getObjectConverter().byteToObject(bytes, clazz);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "object_";
    }

    private void checkObjectConverter()
    {
        if (getDiskInfo().getObjectConverter() == null)
            throw new NullPointerException("you must provide an ObjectConverter instance before this");
    }
}
