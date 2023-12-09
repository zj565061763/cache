package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Int处理类
 */
internal class IntHandler(info: CacheInfo) : BaseCacheHandler<Int>(info, "integer") {
    override fun encodeToByteImpl(value: Int, clazz: Class<*>?): ByteArray {
        return value.toString().toByteArray()
    }

    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Int {
        return String(bytes).toInt()
    }
}