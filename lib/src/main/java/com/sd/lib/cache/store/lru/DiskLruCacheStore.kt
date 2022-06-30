package com.sd.lib.cache.store.lru

import com.sd.lib.cache.store.UnlimitedDiskCacheStore
import java.io.File

/**
 * Lru算法的磁盘缓存
 */
class DiskLruCacheStore @JvmOverloads constructor(
    /** 最大数量或者大小 */
    maxSize: Int,
    /** 保存目录 */
    directory: File,
    /** true-限制数量，false-限制大小 */
    limitCount: Boolean = true,
) : LruCacheStore(maxSize) {

    private val _store = UnlimitedDiskCacheStore(directory)
    private val _limitCount = limitCount

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

    override fun getLruCacheMap(): Map<String, Int>? {
        return _store.getCacheFiles()?.associateBy(
            keySelector = { it.name },
            valueTransform = { it.length().toInt() },
        )
    }

    override fun sizeOfLruCacheEntry(key: String, byteCount: Int): Int {
        return if (_limitCount) 1 else byteCount
    }

    @Throws(Exception::class)
    override fun onLruCacheEntryEvicted(key: String) {
        _store.getCacheFileByName(key)?.delete()
    }

    @Throws(Exception::class)
    override fun transformKeyForLruCache(key: String): String {
        return _store.transformKey(key)
    }
}