package com.sd.lib.cache.handler.impl;

import com.sd.lib.cache.CacheInfo;
import com.sd.lib.cache.handler.BaseCacheHandler;

/**
 * Boolean处理类
 */
public class BooleanHandler extends BaseCacheHandler<Boolean> {
    public BooleanHandler(CacheInfo info) {
        super(info);
    }

    @Override
    protected byte[] valueToByte(Boolean value) {
        return value.toString().getBytes();
    }

    @Override
    protected Boolean byteToValue(byte[] bytes, Class<?> clazz) {
        return Boolean.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix() {
        return "boolean_";
    }
}
