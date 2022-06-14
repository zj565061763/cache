package com.sd.lib.cache.store

import java.io.File

/**
 * 无限制的磁盘缓存
 */
open class UnlimitedDiskCacheStore(directory: File) : BaseDiskCacheStore(directory) {

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