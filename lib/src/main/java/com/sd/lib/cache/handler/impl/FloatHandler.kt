package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Float处理类
 */
internal class FloatHandler(info: CacheInfo) : BaseCacheHandler<Float>(info, "float") {
    override fun encodeToByteImpl(value: Float, clazz: Class<*>?): ByteArray {
        return value.toString().toByteArray()
    }

    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Float {
        return String(bytes).toFloat()
    }
}