package com.sd.lib.cache.handler.impl;

import com.sd.lib.cache.CacheInfo;
import com.sd.lib.cache.handler.BaseCacheHandler;

/**
 * Double处理类
 */
public class DoubleHandler extends BaseCacheHandler<Double> {
    public DoubleHandler(CacheInfo info) {
        super(info);
    }

    @Override
    protected byte[] valueToByte(Double value) {
        return value.toString().getBytes();
    }

    @Override
    protected Double byteToValue(byte[] bytes, Class<?> clazz) {
        return Double.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix() {
        return "double_";
    }
}
