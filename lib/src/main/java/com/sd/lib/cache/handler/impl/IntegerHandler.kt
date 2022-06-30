package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Integer处理类
 */
internal class IntegerHandler(info: CacheInfo) : BaseCacheHandler<Int>(info, "integer") {
    override fun encodeToByteImpl(value: Int): ByteArray {
        return value.toString().toByteArray()
    }

    override fun byteToValue(bytes: ByteArray, clazz: Class<*>?): Int {
        return String(bytes).toInt()
    }
}