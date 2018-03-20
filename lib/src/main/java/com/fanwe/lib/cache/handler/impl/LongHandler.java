package com.fanwe.lib.cache.handler.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.api.ICommonCache;
import com.fanwe.lib.cache.handler.StringConverterHandler;

/**
 * Long处理类
 */
public class LongHandler extends StringConverterHandler<Long> implements ICommonCache<Long>
{
    public LongHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(Long value)
    {
        return String.valueOf(value);
    }

    @Override
    protected Long stringToValue(String string, Class<Long> clazz)
    {
        return Long.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "long_";
    }

    @Override
    public boolean put(String key, Long value)
    {
        return putCache(key, value);
    }

    @Override
    public Long get(String key)
    {
        return getCache(key, null);
    }
}
