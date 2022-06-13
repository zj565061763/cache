package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Double处理类
 */
internal class DoubleHandler(info: CacheInfo) : BaseCacheHandler<Double>(info) {
    override fun valueToByte(value: Double): ByteArray {
        return value.toString().toByteArray()
    }

    override fun byteToValue(bytes: ByteArray, clazz: Class<*>?): Double {
        return String(bytes).toDouble()
    }

    override val keyPrefix: String
        get() = "double_"
}