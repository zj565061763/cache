package com.sd.lib.cache.store.lru

import com.sd.lib.cache.store.UnlimitedDiskCacheStore
import java.io.File

/**
 * Lru算法的磁盘缓存
 */
abstract class DiskLruCacheStore(
    maxSize: Int,
    directory: File,
) : LruCacheStore(maxSize) {

    private val _store = UnlimitedDiskCacheStore(directory)

    final override fun putCacheImpl(key: String, value: ByteArray): Boolean {
        return _store.putCache(key, value)
    }

    final override fun getCacheImpl(key: String): ByteArray? {
        return _store.getCache(key)
    }

    final override fun removeCacheImpl(key: String): Boolean {
        return _store.removeCache(key)
    }

    final override fun containsCacheImpl(key: String): Boolean {
        return _store.containsCache(key)
    }

    final override fun getLruCacheInitKeys(): List<String>? {
        return _store.getCacheFiles()?.map { it.name }
    }

    final override fun sizeOfLruCacheEntry(key: String): Int {
        val file = _store.getCacheFileByName(key) ?: return 0
        return onLruCacheSizeOfEntry(file)
    }

    @Throws(Exception::class)
    final override fun onLruCacheEntryEvicted(key: String) {
        _store.getCacheFileByName(key)?.delete()
    }

    @Throws(Exception::class)
    final override fun transformKeyForLruCache(key: String): String {
        return _store.transformKey(key)
    }

    protected abstract fun onLruCacheSizeOfEntry(file: File): Int
}