package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * String处理类
 */
public class StringHandler extends ByteConverterHandler<String>
{
    public StringHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(String value)
    {
        return value.getBytes();
    }

    @Override
    protected String byteToValue(byte[] bytes, Class<String> clazz)
    {
        return new String(bytes);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "string_";
    }
}
