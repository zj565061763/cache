package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Double处理类
 */
internal class DoubleHandler(info: CacheInfo) : BaseCacheHandler<Double>(info, "double") {
    override fun encodeToByteImpl(value: Double): ByteArray {
        return value.toString().toByteArray()
    }

    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Double {
        return String(bytes).toDouble()
    }
}