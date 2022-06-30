package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * String处理类
 */
internal class StringHandler(info: CacheInfo) : BaseCacheHandler<String>(info, "string") {
    override fun encodeToByteImpl(value: String): ByteArray {
        return value.toByteArray()
    }

    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): String {
        return String(bytes)
    }
}