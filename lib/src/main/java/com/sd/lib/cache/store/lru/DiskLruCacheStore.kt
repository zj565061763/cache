package com.sd.lib.cache.store.lru

import com.sd.lib.cache.store.UnlimitedDiskCacheStore
import java.io.File

/**
 * Lru算法的磁盘缓存
 */
abstract class DiskLruCacheStore internal constructor(
    limit: Int,
    directory: File,
) : LruCacheStore(limit) {

    private val _store = UnlimitedDiskCacheStore(directory)

    // -------------------- Basic start --------------------

    override fun putCacheImpl(key: String, value: ByteArray): Boolean {
        return _store.putCache(key, value)
    }

    override fun getCacheImpl(key: String): ByteArray? {
        return _store.getCache(key)
    }

    override fun removeCacheImpl(key: String): Boolean {
        return _store.removeCache(key)
    }

    override fun containsCacheImpl(key: String): Boolean {
        return _store.containsCache(key)
    }

    // -------------------- Basic end --------------------

    override fun getLruCacheSizeMap(): Map<String, Int>? {
        return _store.getCacheSizeMap()
    }

    @Throws(Exception::class)
    override fun onLruCacheEntryEvicted(key: String) {
        _store.removeCacheByFilename(key)
    }

    @Throws(Exception::class)
    override fun transformKeyForLruCache(key: String): String {
        return _store.transformKey(key)
    }

    companion object {
        /**
         * 限制数量为[count]的缓存
         */
        @JvmStatic
        fun limitCount(count: Int, directory: File): DiskLruCacheStore {
            return CountDiskLruCacheStore(count, directory)
        }

        /**
         * 限制大小为[count]的缓存，单位B
         */
        @JvmStatic
        fun limitSize(count: Int, directory: File): DiskLruCacheStore {
            return SizeDiskLruCacheStore(count, directory)
        }
    }
}

private class CountDiskLruCacheStore(
    limit: Int,
    directory: File,
) : DiskLruCacheStore(limit, directory) {
    override fun sizeOfLruCacheEntry(key: String, byteCount: Int): Int {
        return 1
    }
}

private class SizeDiskLruCacheStore(
    limit: Int,
    directory: File,
) : DiskLruCacheStore(limit, directory) {
    override fun sizeOfLruCacheEntry(key: String, byteCount: Int): Int {
        return byteCount
    }
}