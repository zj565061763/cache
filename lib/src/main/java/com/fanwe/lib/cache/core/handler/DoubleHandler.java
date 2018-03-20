package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;

/**
 * Double处理类
 */
public class DoubleHandler extends AbstractStringHandler<Double>
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
    protected Double stringToValue(String string)
    {
        return Double.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "double_";
    }
}
