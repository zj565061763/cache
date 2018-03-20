package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;
import com.fanwe.lib.cache.core.api.ICommonCache;

/**
 * Boolean处理类
 */
public class BooleanHandler extends StringConverterHandler<Boolean> implements ICommonCache<Boolean>
{
    public BooleanHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String valueToString(Boolean value)
    {
        return String.valueOf(value);
    }

    @Override
    protected Boolean stringToValue(String string, Class<Boolean> clazz)
    {
        return Boolean.valueOf(string);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "boolean_";
    }

    @Override
    public boolean put(String key, Boolean value)
    {
        return putCache(key, value);
    }

    @Override
    public Boolean get(String key)
    {
        return getCache(key, null);
    }
}
