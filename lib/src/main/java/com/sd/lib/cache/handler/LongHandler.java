package com.sd.lib.cache.handler;

import com.sd.lib.cache.DiskInfo;

/**
 * Long处理类
 */
public class LongHandler extends BytesConverterHandler<Long>
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
    protected Long byteToValue(byte[] bytes, Class clazz)
    {
        return Long.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix()
    {
        return "long_";
    }
}
