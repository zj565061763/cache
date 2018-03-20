package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;

/**
 * Boolean处理类
 */
public class BooleanHandler extends StringConverterHandler<Boolean>
{
    public BooleanHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(Boolean value)
    {
        return String.valueOf(value);
    }

    @Override
    protected Boolean stringToValue(String string, Class<Boolean> clazz)
    {
        return Boolean.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "boolean_";
    }
}
