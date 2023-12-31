package com.sd.lib.cache.store

import android.content.Context
import android.util.LruCache
import java.io.File

/**
 * 限制个数的LRU算法仓库
 */
internal fun CacheStore.limitCount(limit: Int): LimitCacheStore {
    return if (this is LimitCountCacheStore) this
    else LimitCountCacheStore(limit, this)
}

/**
 * 限制大小的LRU算法仓库
 */
internal interface LimitCacheStore : CacheStore {
    /**
     * 限制大小
     */
    fun limit(limit: Int)
}

/**
 * 限制个数的LRU算法仓库
 */
private class LimitCountCacheStore(
    limit: Int,
    store: CacheStore,
) : LruCacheStore(limit, store) {
    override fun sizeOfEntry(key: String, value: ByteArray?): Int = 1
}

private abstract class LruCacheStore protected constructor(
    limit: Int,
    private val store: CacheStore,
) : LimitCacheStore, AutoCloseable {

    init {
        check(limit > 0)
    }

    private val _lruCache = object : LruCache<String, Int>(limit) {
        override fun sizeOf(key: String, value: Int?): Int {
            return value ?: 0
        }

        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Int?, newValue: Int?) {
            if (evicted) {
                store.removeCache(key)
            }
        }
    }

    protected abstract fun sizeOfEntry(key: String, value: ByteArray?): Int

    /**
     * 限制大小
     */
    final override fun limit(limit: Int) {
        check(limit > 0)
        if (_lruCache.maxSize() != limit) {
            _lruCache.resize(limit)
        }
    }

    final override fun init(context: Context, directory: File, id: String) {
        store.init(context, directory, id)
        allKeys()?.forEach { key ->
            val size = sizeOfEntry(key, null)
            _lruCache.put(key, size)
        }
    }

    final override fun putCache(key: String, value: ByteArray): Boolean {
        return store.putCache(key, value).also { result ->
            if (result) {
                val size = sizeOfEntry(key, value)
                _lruCache.put(key, size)
            }
        }
    }

    final override fun getCache(key: String): ByteArray? {
        _lruCache.get(key)
        return store.getCache(key)
    }

    final override fun removeCache(key: String) {
        _lruCache.remove(key)
        store.removeCache(key)
    }

    final override fun containsCache(key: String): Boolean {
        return store.containsCache(key)
    }

    final override fun allKeys(): Array<String>? {
        return store.allKeys()
    }

    final override fun close() {
        if (store is AutoCloseable) {
            store.close()
        }
    }
}