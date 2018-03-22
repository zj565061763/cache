package com.fanwe.lib.cache.handler.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.handler.StringConverterHandler;

/**
 * Float处理类
 */
public class FloatHandler extends StringConverterHandler<Float>
{
    public FloatHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(Float value)
    {
        return value.toString();
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
}
