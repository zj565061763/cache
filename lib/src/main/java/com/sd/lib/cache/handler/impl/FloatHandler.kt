package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Float处理类
 */
internal class FloatHandler(info: CacheInfo) : BaseCacheHandler<Float>(info) {
    override fun valueToByte(value: Float): ByteArray {
        return value.toString().toByteArray()
    }

    override fun byteToValue(bytes: ByteArray, clazz: Class<*>?): Float {
        return String(bytes).toFloat()
    }

    override val keyPrefix: String
        get() = "float_"
}