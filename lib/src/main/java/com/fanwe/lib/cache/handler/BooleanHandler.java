package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Boolean处理类
 */
public class BooleanHandler extends StringConverterHandler<Boolean>
{
    public BooleanHandler(DiskInfo diskInfo)
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
