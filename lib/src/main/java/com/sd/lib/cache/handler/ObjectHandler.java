package com.sd.lib.cache.handler;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;

/**
 * Object处理类
 */
public abstract class ObjectHandler extends BaseCacheHandler<Object>
{
    public ObjectHandler(CacheInfo info)
    {
        super(info);
    }

    @Override
    protected byte[] valueToByte(Object value)
    {
        return getObjectConverter().objectToByte(value);
    }

    @Override
    protected Object byteToValue(byte[] bytes, Class<?> clazz)
    {
        return getObjectConverter().byteToObject(bytes, clazz);
    }

    private Cache.ObjectConverter getObjectConverter()
    {
        final Cache.ObjectConverter converter = getCacheInfo().getObjectConverter();
        if (converter == null)
            throw new NullPointerException("you must provide an ObjectConverter instance before this");
        return converter;
    }
}
