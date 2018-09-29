package com.sd.lib.cache.handler;

import com.sd.lib.cache.Disk;
import com.sd.lib.cache.DiskInfo;

/**
 * Object处理类
 */
public class ObjectHandler extends ByteConverterHandler<Object> implements Disk.ObjectCache
{
    public ObjectHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(Object value)
    {
        return getObjectConverter().objectToByte(value);
    }

    @Override
    protected Object byteToValue(byte[] bytes, Class clazz)
    {
        return getObjectConverter().byteToObject(bytes, clazz);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "object_";
    }

    private Disk.ObjectConverter getObjectConverter()
    {
        final Disk.ObjectConverter converter = getDiskInfo().getObjectConverter();
        if (converter == null)
            throw new NullPointerException("you must provide an ObjectConverter instance before this");
        return converter;
    }

    @Override
    public boolean put(Object value)
    {
        final String key = value.getClass().getName();
        return putCache(key, value);
    }

    @Override
    public <T> T get(Class<T> clazz)
    {
        final String key = clazz.getName();
        return (T) getCache(key, clazz);
    }

    @Override
    public boolean remove(Class clazz)
    {
        final String key = clazz.getName();
        return removeCache(key);
    }
}
