package com.sd.lib.cache.handler

import com.sd.lib.cache.CacheException
import com.sd.lib.cache.CacheInfo

/**
 * Object处理类
 */
internal class ObjectHandler<T>(info: CacheInfo, keyPrefix: String) : BaseCacheHandler<T>(info, keyPrefix) {
    override fun encode(value: T, clazz: Class<T>?): ByteArray {
        if (clazz == null) throw CacheException("class is null")
        return cacheInfo.objectConverter.encode(value, clazz)
    }

    override fun decode(bytes: ByteArray, clazz: Class<T>?): T {
        if (clazz == null) throw CacheException("class is null")
        return cacheInfo.objectConverter.decode(bytes, clazz)
    }
}