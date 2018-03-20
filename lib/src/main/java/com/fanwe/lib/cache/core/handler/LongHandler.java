package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;

/**
 * Long处理类
 */
public class LongHandler extends AbstractStringHandler<Long>
{
    public LongHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(Long value)
    {
        return String.valueOf(value);
    }

    @Override
    protected Long stringToValue(String string)
    {
        return Long.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "long_";
    }
}
