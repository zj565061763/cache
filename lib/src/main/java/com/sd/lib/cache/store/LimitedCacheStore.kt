package com.sd.lib.cache.store

import android.content.Context
import android.util.LruCache
import java.io.File

/**
 * 限制大小的LRU算法仓库，单位MB
 */
internal class LimitedCacheStore(
    limitMB: Int,
    private val store: CacheStore,
) : CacheStore, AutoCloseable {

    init {
        check(limitMB > 0)
    }

    private val _lruCache = object : LruCache<String, Int>(limitMB.toByteCount()) {
        override fun sizeOf(key: String, value: Int?): Int {
            return value ?: 0
        }

        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Int?, newValue: Int?) {
            if (evicted) {
                store.removeCache(key)
            }
        }
    }

    /**
     * 限制大小，单位MB
     */
    fun limitMB(limitMB: Int) {
        val limit = limitMB.toByteCount()
        if (_lruCache.maxSize() != limit) {
            _lruCache.resize(limit)
        }
    }

    override fun init(context: Context, directory: File) {
        store.init(context, directory)
        keys()?.forEach { _lruCache.put(it, sizeOf(it)) }
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        return store.putCache(key, value).also { result ->
            if (result) _lruCache.put(key, value.size)
        }
    }

    override fun getCache(key: String): ByteArray? {
        _lruCache.get(key)
        return store.getCache(key)
    }

    override fun removeCache(key: String) {
        _lruCache.remove(key)
        store.removeCache(key)
    }

    override fun containsCache(key: String): Boolean {
        return store.containsCache(key)
    }

    override fun keys(): Array<String>? {
        return store.keys()
    }

    override fun sizeOf(key: String): Int {
        return store.sizeOf(key)
    }

    override fun close() {
        if (store is AutoCloseable) {
            store.close()
        }
    }
}

/**
 * MB -> B
 */
private fun Int.toByteCount(): Int = this * 1024 * 1024