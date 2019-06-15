package com.sd.lib.cache.handler;

import com.sd.lib.cache.CacheInfo;

/**
 * Long处理类
 */
public class LongHandler extends BaseCacheHandler<Long>
{
    public LongHandler(CacheInfo info)
    {
        super(info);
    }

    @Override
    protected byte[] valueToByte(Long value)
    {
        return value.toString().getBytes();
    }

    @Override
    protected Long byteToValue(byte[] bytes, Class<?> clazz)
    {
        return Long.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix()
    {
        return "long_";
    }
}
