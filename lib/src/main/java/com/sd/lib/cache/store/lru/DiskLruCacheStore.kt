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

    val _store = UnlimitedDiskCacheStore(directory)

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

    final override fun onLruCacheInitKeys(): List<String> {
        return _store.getCacheFiles().map {
            _store.unpackKey(it.name)
        }
    }

    final override fun onLruCacheSizeOfEntry(key: String): Int {
        val file = _store.getCacheFileOrNull(key) ?: return 0
        return onLruCacheSizeOfEntry(file)
    }

    final override fun onLruCacheEntryEvicted(key: String) {
        _store.removeCache(key)
    }

    protected abstract fun onLruCacheSizeOfEntry(file: File): Int
}