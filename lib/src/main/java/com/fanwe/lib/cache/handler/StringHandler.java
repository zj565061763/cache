package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * String处理类
 */
public class StringHandler extends StringConverterHandler<String>
{
    public StringHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
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
