package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Long处理类
 */
public class LongHandler extends StringConverterHandler<Long>
{
    public LongHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected Long stringToValue(String string, Class<Long> clazz)
    {
        return Long.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "long_";
    }
}
