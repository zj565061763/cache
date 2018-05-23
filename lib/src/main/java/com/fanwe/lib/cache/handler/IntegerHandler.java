package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Integer处理类
 */
public class IntegerHandler extends StringConverterHandler<Integer>
{
    public IntegerHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
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
