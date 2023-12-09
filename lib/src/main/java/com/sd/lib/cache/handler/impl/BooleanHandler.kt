package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Boolean处理类
 */
internal class BooleanHandler(info: CacheInfo) : BaseCacheHandler<Boolean>(info, "boolean") {
    override fun encodeToByteImpl(value: Boolean, clazz: Class<*>?): ByteArray {
        return value.toString().toByteArray()
    }

    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Boolean {
        return String(bytes).toBooleanStrict()
    }
}