package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;
import com.fanwe.lib.cache.core.api.ICommonCache;

/**
 * Float处理类
 */
public class FloatHandler extends StringConverterHandler<Float> implements ICommonCache<Float>
{
    public FloatHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(Float value)
    {
        return String.valueOf(value);
    }

    @Override
    protected Float stringToValue(String string, Class<Float> clazz)
    {
        return Float.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "float_";
    }

    @Override
    public boolean put(String key, Float value)
    {
        return putCache(key, value);
    }

    @Override
    public Float get(String key)
    {
        return getCache(key, null);
    }
}
