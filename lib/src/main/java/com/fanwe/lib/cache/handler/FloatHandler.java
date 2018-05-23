package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Float处理类
 */
public class FloatHandler extends StringConverterHandler<Float>
{
    public FloatHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
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
