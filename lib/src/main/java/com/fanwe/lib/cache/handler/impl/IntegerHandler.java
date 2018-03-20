package com.fanwe.lib.cache.handler.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.api.ICommonCache;
import com.fanwe.lib.cache.handler.StringConverterHandler;

/**
 * Integer处理类
 */
public class IntegerHandler extends StringConverterHandler<Integer> implements ICommonCache<Integer>
{
    public IntegerHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(Integer value)
    {
        return String.valueOf(value);
    }

    @Override
    protected Integer stringToValue(String string, Class<Integer> clazz)
    {
        return Integer.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "integer_";
    }

    @Override
    public boolean put(String key, Integer value)
    {
        return putCache(key, value);
    }

    @Override
    public Integer get(String key)
    {
        return getCache(key, null);
    }
}
