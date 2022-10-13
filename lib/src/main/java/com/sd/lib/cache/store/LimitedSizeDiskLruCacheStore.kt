package com.sd.lib.cache.store

import com.sd.lib.cache.Cache
import com.sd.lib.lrucache.FDiskLruCache
import java.io.File

/**
 * 限制大小的Lru算法磁盘缓存
 */
class LimitedSizeDiskLruCacheStore(
    directory: File,
    limitMB: Int = 500,
) : Cache.CacheStore {

    private val _cache by lazy {
        val mb = limitMB * 1024 * 1024L
        FDiskLruCache.dir(directory, mb)
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        return _cache.edit(key) { editFile ->
            editFile.outputStream().buffered().use {
                it.write(value)
                true
            }
        }
    }

    override fun getCache(key: String): ByteArray? {
        val file = _cache.get(key) ?: return null
        return file.inputStream().buffered().use {
            it.readBytes()
        }
    }

    override fun removeCache(key: String): Boolean {
        return _cache.remove(key)
    }

    override fun containsCache(key: String): Boolean {
        val file = _cache.get(key) ?: return false
        return file.exists()
    }
}