package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Long处理类
 */
public class LongHandler extends ByteConverterHandler<Long>
{
    public LongHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(Long value)
    {
        return value.toString().getBytes();
    }

    @Override
    protected Long byteToValue(byte[] bytes, Class<Long> clazz)
    {
        return Long.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix()
    {
        return "long_";
    }
}
