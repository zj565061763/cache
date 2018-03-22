package com.fanwe.lib.cache.handler.impl;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.handler.StringConverterHandler;

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
