package com.sd.lib.cache.store

import java.io.File

/**
 * 文件缓存实现类
 */
open class SimpleDiskCacheStore(directory: File) : BaseDiskCacheStore(directory) {

    override fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean {
        return file.outputStream().buffered().use { outputStream ->
            outputStream.write(value)
            true
        }
    }

    override fun getCacheImpl(key: String, clazz: Class<*>, file: File): ByteArray {
        return file.inputStream().buffered().use { inputStream ->
            inputStream.readBytes()
        }
    }
}