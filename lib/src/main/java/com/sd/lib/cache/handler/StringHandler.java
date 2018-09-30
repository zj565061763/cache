package com.sd.lib.cache.handler;

import com.sd.lib.cache.CacheInfo;

/**
 * String处理类
 */
public class StringHandler extends BaseCacheHandler<String>
{
    public StringHandler(CacheInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(String value)
    {
        return value.getBytes();
    }

    @Override
    protected String byteToValue(byte[] bytes, Class clazz)
    {
        return new String(bytes);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "string_";
    }
}
