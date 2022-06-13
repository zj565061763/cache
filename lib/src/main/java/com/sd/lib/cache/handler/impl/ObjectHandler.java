package com.sd.lib.cache.handler.impl;

import com.sd.lib.cache.CacheInfo;
import com.sd.lib.cache.handler.BaseCacheHandler;

/**
 * Object处理类
 */
public abstract class ObjectHandler extends BaseCacheHandler<Object> {
    public ObjectHandler(CacheInfo info) {
        super(info);
    }

    @Override
    protected byte[] valueToByte(Object value) throws Exception {
        return getCacheInfo().getObjectConverter().objectToByte(value);
    }

    @Override
    protected Object byteToValue(byte[] bytes, Class<?> clazz) throws Exception {
        return getCacheInfo().getObjectConverter().byteToObject(bytes, clazz);
    }
}
