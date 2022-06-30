package com.sd.lib.cache.store.lru

import android.os.Looper
import com.sd.lib.cache.store.UnlimitedDiskCacheStore
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlin.concurrent.thread

/**
 * Lru算法的磁盘缓存
 */
abstract class DiskLruCacheStore internal constructor(
    limit: Int,
    directory: File,
) : BaseLruCacheStore(limit) {

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

    @Throws(Exception::class)
    override fun getLruCacheSizeMap(): Map<String, Int>? {
        return _store.getCacheSizeMap()
    }

    override fun onLruCacheEntryEvicted(key: String) {
        launch(Dispatchers.IO) {

        }

        val isMain = Looper.myLooper() == Looper.getMainLooper()
        if (isMain) {
            thread {
                runCatching {
                    _store.removeCacheByFilename(key)
                    logMsg("removeCacheByFilename thread new ${Thread.currentThread().name}")
                }
            }
        } else {
            _store.removeCacheByFilename(key)
            logMsg("removeCacheByFilename thread ${Thread.currentThread().name}")
        }
    }

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
         * 限制大小为[size]的缓存，单位MB
         */
        @JvmStatic
        fun limitSize(size: Int, directory: File): DiskLruCacheStore {
            val byteSize = size * 1024 * 1024
            return SizeDiskLruCacheStore(byteSize, directory)
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