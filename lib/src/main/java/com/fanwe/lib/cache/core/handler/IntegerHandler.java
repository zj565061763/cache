package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;

/**
 * Integer处理类
 */
public class IntegerHandler extends AbstractStringHandler<Integer>
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
    protected Integer stringToValue(String string)
    {
        return Integer.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "integer_";
    }
}
