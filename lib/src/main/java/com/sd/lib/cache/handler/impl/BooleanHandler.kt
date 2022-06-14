package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * Boolean处理类
 */
internal class BooleanHandler(info: CacheInfo) : BaseCacheHandler<Boolean>(info, "boolean") {
    override fun valueToByte(value: Boolean): ByteArray {
        return value.toString().toByteArray()
    }

    override fun byteToValue(bytes: ByteArray, clazz: Class<*>?): Boolean {
        return String(bytes).toBooleanStrict()
    }
}