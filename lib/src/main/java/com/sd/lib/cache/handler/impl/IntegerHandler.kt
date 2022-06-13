package com.sd.lib.cache.handler.impl;

import com.sd.lib.cache.CacheInfo;
import com.sd.lib.cache.handler.BaseCacheHandler;

/**
 * Integer处理类
 */
public class IntegerHandler extends BaseCacheHandler<Integer> {
    public IntegerHandler(CacheInfo info) {
        super(info);
    }

    @Override
    protected byte[] valueToByte(Integer value) {
        return value.toString().getBytes();
    }

    @Override
    protected Integer byteToValue(byte[] bytes, Class<?> clazz) {
        return Integer.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix() {
        return "integer_";
    }
}
