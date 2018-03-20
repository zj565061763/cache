package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;
import com.fanwe.lib.cache.core.api.ICommonCache;

/**
 * Double处理类
 */
public class DoubleHandler extends StringConverterHandler<Double> implements ICommonCache<Double>
{
    public DoubleHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(Double value)
    {
        return String.valueOf(value);
    }

    @Override
    protected Double stringToValue(String string, Class<Double> clazz)
    {
        return Double.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "double_";
    }

    @Override
    public boolean put(String key, Double value)
    {
        return putCache(key, value);
    }

    @Override
    public Double get(String key)
    {
        return getCache(key, null);
    }
}
