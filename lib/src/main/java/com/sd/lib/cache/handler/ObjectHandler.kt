package com.sd.lib.cache.handler

import com.sd.lib.cache.CacheException
import com.sd.lib.cache.libError

/**
 * Object处理类
 */
internal class ObjectHandler<T>(info: CacheInfo, handlerKey: String) : BaseCacheHandler<T>(info, handlerKey) {
    override fun encode(value: T, clazz: Class<T>?): ByteArray {
        if (clazz == null) libError("class is null")
        return cacheInfo.objectConverter.encode(value, clazz).also {
            if (it.isEmpty()) {
                throw CacheException("Converter encode returns empty ${clazz.name}")
            }
        }
    }

    override fun decode(bytes: ByteArray, clazz: Class<T>?): T? {
        if (clazz == null) libError("class is null")
        if (bytes.isEmpty()) return null
        return cacheInfo.objectConverter.decode(bytes, clazz)
    }
}