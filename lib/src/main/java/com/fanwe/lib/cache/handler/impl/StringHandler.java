package com.fanwe.lib.cache.handler.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.handler.StringConverterHandler;

/**
 * String处理类
 */
public class StringHandler extends StringConverterHandler<String>
{
    public StringHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(String value)
    {
        return value;
    }

    @Override
    protected String stringToValue(String string, Class<String> clazz)
    {
        return string;
    }

    @Override
    protected String getKeyPrefix()
    {
        return "string_";
    }
}
