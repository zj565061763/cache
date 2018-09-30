package com.sd.lib.cache.handler;

import com.sd.lib.cache.CacheInfo;

/**
 * byte数组处理类
 */
public class BytesHandler extends BaseCacheHandler<byte[]>
{
    public BytesHandler(CacheInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(byte[] value)
    {
        return value;
    }

    @Override
    protected byte[] byteToValue(byte[] bytes, Class clazz)
    {
        return bytes;
    }

    @Override
    protected String getKeyPrefix()
    {
        return "bytes_";
    }
}
