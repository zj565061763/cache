package com.sd.lib.cache.handler.impl;

import com.sd.lib.cache.CacheInfo;
import com.sd.lib.cache.handler.BaseCacheHandler;

/**
 * byte数组处理类
 */
public class BytesHandler extends BaseCacheHandler<byte[]> {
    public BytesHandler(CacheInfo info) {
        super(info);
    }

    @Override
    protected byte[] valueToByte(byte[] value) {
        return value;
    }

    @Override
    protected byte[] byteToValue(byte[] bytes, Class<?> clazz) {
        return bytes;
    }

    @Override
    protected String getKeyPrefix() {
        return "bytes_";
    }
}
