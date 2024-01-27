package com.sd.lib.cache.handler

import com.sd.lib.cache.CacheError

/**
 * Object处理类
 */
internal class ObjectHandler<T>(info: CacheInfo, handlerKey: String) : BaseCacheHandler<T>(info, handlerKey) {
    override fun encode(value: T, clazz: Class<T>?): ByteArray {
        if (clazz == null) throw CacheError("class is null")
        return cacheInfo.objectConverter.encode(value, clazz).also {
            if (it.isEmpty()) error("Converter encode returns empty ${clazz.name}")
        }
    }

    override fun decode(bytes: ByteArray, clazz: Class<T>?): T? {
        if (clazz == null) throw CacheError("class is null")
        if (bytes.isEmpty()) return null
        return cacheInfo.objectConverter.decode(bytes, clazz)
    }
}