package com.sd.lib.cache.handler

import com.sd.lib.cache.CacheException
import com.sd.lib.cache.CacheInfo

/**
 * Object处理类
 */
internal class ObjectHandler(info: CacheInfo, keyPrefix: String) : BaseCacheHandler<Any>(info, keyPrefix) {
    override fun encodeToByteImpl(value: Any, clazz: Class<*>?): ByteArray {
        if (clazz == null) throw CacheException("class is null")
        return cacheInfo.objectConverter.objectToByte(value, clazz)
    }

    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Any {
        if (clazz == null) throw CacheException("class is null")
        return cacheInfo.objectConverter.byteToObject(bytes, clazz)
    }
}