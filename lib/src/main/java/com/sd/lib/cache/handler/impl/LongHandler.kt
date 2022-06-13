package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Long处理类
 */
internal class LongHandler(info: CacheInfo) : BaseCacheHandler<Long>(info) {
    override fun valueToByte(value: Long): ByteArray {
        return value.toString().toByteArray()
    }

    override fun byteToValue(bytes: ByteArray, clazz: Class<*>?): Long {
        return String(bytes).toLong()
    }

    override val keyPrefix: String
        get() = "long_"
}