package com.fanwe.lib.cache.handler.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.handler.StringConverterHandler;

/**
 * Integer处理类
 */
public class IntegerHandler extends StringConverterHandler<Integer>
{
    public IntegerHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(Integer value)
    {
        return value.toString();
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
}
