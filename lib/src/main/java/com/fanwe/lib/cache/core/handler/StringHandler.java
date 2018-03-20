package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;

/**
 * String处理类
 */
public class StringHandler extends AbstractStringHandler<String>
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
    protected String stringToValue(String string)
    {
        return string;
    }

    @Override
    protected String getKeyPrefix()
    {
        return "string_";
    }
}
