package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Object处理类
 */
internal abstract class ObjectHandler(info: CacheInfo) : BaseCacheHandler<Any>(info) {
    override fun valueToByte(value: Any): ByteArray {
        return cacheInfo.objectConverter.objectToByte(value)
    }

    override fun byteToValue(bytes: ByteArray, clazz: Class<*>?): Any {
        requireNotNull(clazz) { "class is null" }
        return cacheInfo.objectConverter.byteToObject(bytes, clazz)
    }
}