package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * String处理类
 */
internal class StringHandler(info: CacheInfo) : BaseCacheHandler<String>(info) {
    override fun valueToByte(value: String): ByteArray {
        return value.toByteArray()
    }

    override fun byteToValue(bytes: ByteArray, clazz: Class<*>?): String {
        return String(bytes)
    }

    override val keyPrefix: String
        get() = "string_"
}