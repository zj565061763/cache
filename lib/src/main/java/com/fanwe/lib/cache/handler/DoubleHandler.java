package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Double处理类
 */
public class DoubleHandler extends StringConverterHandler<Double>
{
    public DoubleHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
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
}
