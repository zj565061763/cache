package com.sd.lib.cache.handler.impl;

import com.sd.lib.cache.CacheInfo;
import com.sd.lib.cache.handler.BaseCacheHandler;

/**
 * Float处理类
 */
public class FloatHandler extends BaseCacheHandler<Float> {
    public FloatHandler(CacheInfo info) {
        super(info);
    }

    @Override
    protected byte[] valueToByte(Float value) {
        return value.toString().getBytes();
    }

    @Override
    protected Float byteToValue(byte[] bytes, Class<?> clazz) {
        return Float.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix() {
        return "float_";
    }
}
