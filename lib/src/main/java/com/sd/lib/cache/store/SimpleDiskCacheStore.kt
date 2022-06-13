package com.sd.lib.cache.store

import java.io.File

/**
 * 文件缓存实现类
 */
open class SimpleDiskCacheStore(directory: File) : BaseDiskCacheStore(directory) {

    @Throws(Exception::class)
    override fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean {
        return file.outputStream().buffered().use {
            it.write(value)
            true
        }
    }

    @Throws(Exception::class)
    override fun getCacheImpl(key: String, file: File): ByteArray {
        return file.inputStream().buffered().use {
            it.readBytes()
        }
    }
}