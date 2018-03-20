package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;

/**
 * Float处理类
 */
public class FloatHandler extends AbstractStringHandler<Float>
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
    protected Float stringToValue(String string)
    {
        return Float.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "float_";
    }
}
