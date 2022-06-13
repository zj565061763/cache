package com.sd.lib.cache.handler;

import com.sd.lib.cache.CacheInfo;

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
