package com.sd.lib.cache.handler

import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheException

internal class ObjectHandler<T>(info: CacheInfo, handlerKey: String) : BaseCacheHandler<T>(info, handlerKey) {
    override fun encode(value: T, clazz: Class<T>, converter: Cache.ObjectConverter): ByteArray {
        return converter.encode(value, clazz)
            .also {
                if (it.isEmpty()) {
                    throw CacheException("Converter encode returns empty ${clazz.name}")
                }
            }
    }

    override fun decode(bytes: ByteArray, clazz: Class<T>, converter: Cache.ObjectConverter): T? {
        if (bytes.isEmpty()) return null
        return converter.decode(bytes, clazz)
    }
}