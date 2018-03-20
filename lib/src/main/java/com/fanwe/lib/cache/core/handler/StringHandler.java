package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;
import com.fanwe.lib.cache.core.api.ICommonCache;

/**
 * String处理类
 */
public class StringHandler extends StringConverterHandler<String> implements ICommonCache<String>
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
    protected String stringToValue(String string, Class<String> clazz)
    {
        return string;
    }

    @Override
    protected String getKeyPrefix()
    {
        return "string_";
    }

    @Override
    public boolean put(String key, String value)
    {
        return putCache(key, value);
    }

    @Override
    public String get(String key)
    {
        return getCache(key, null);
    }
}
