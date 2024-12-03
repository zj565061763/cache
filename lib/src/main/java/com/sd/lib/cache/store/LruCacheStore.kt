package com.sd.lib.cache.store

import android.content.Context
import android.util.LruCache
import java.io.File

/**
 * 限制个数的LRU算法仓库
 */
internal fun CacheStore.limitCount(count: Int): CacheStore {
    val store = this
    return if (store is LimitCountStore) store
    else LimitCountStore(store = store, limit = count)
}

/**
 * 限制个数的LRU算法仓库
 */
private class LimitCountStore(
    store: CacheStore,
    limit: Int,
) : LruCacheStore(store, limit) {
    override fun sizeOfEntry(key: String, value: ByteArray?): Int = 1
}

private abstract class LruCacheStore(
    private val store: CacheStore,
    limit: Int,
) : CacheStore by store {
    private val _lruCache = object : LruCache<String, Int>(limit) {
        override fun sizeOf(key: String, value: Int): Int {
            return value
        }

        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Int, newValue: Int?) {
            if (evicted) {
                store.removeCache(key)
            }
        }
    }

    final override fun init(
        context: Context,
        directory: File,
        group: String,
        id: String,
    ) {
        store.init(
            context = context,
            directory = directory,
            group = group,
            id = id,
        )
        keys().forEach { key ->
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

    protected abstract fun sizeOfEntry(key: String, value: ByteArray?): Int
}