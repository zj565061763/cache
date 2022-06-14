package com.sd.lib.cache.handler.impl

import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.BaseCacheHandler

/**
 * 字节数组处理类
 */
internal class BytesHandler(info: CacheInfo) : BaseCacheHandler<ByteArray>(info, "bytes") {
    override fun valueToByte(value: ByteArray): ByteArray {
        return value
    }

    override fun byteToValue(bytes: ByteArray, clazz: Class<*>?): ByteArray {
        return bytes
    }
}