package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Long处理类
 */
internal class LongHandler(info: CacheInfo) : BaseCacheHandler<Long>(info, "long") {
    override fun encodeToByteImpl(value: Long, clazz: Class<*>?): ByteArray {
        return value.toString().toByteArray()
    }

    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Long {
        return String(bytes).toLong()
    }
}