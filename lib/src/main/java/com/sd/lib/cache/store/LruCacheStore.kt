package com.sd.lib.cache.store

import android.content.Context
import android.util.LruCache
import java.io.File

/**
 * 限制个数的LRU算法仓库
 */
internal fun CacheStore.limitCount(count: Int): CacheStore {
    require(count > 0)
    val store = this
    return if (store is LimitCountStore) {
        store.also { it.setMaxSize(count) }
    } else {
        LimitCountStore(store = store, maxSize = count)
    }
}

/**
 * 限制个数的LRU算法仓库
 */
private class LimitCountStore(
    store: CacheStore,
    maxSize: Int,
) : LruCacheStore(store, maxSize) {
    override fun sizeOfEntry(key: String, value: ByteArray?): Int = 1
}

private abstract class LruCacheStore(
    private val store: CacheStore,
    maxSize: Int,
) : CacheStore by store {
    private val _lruCache = object : LruCache<String, Int>(maxSize) {
        override fun sizeOf(key: String, value: Int): Int {
            return value
        }

        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Int, newValue: Int?) {
            if (evicted) {
                store.removeCache(key)
            }
        }
    }

    fun setMaxSize(maxSize: Int) {
        if (_lruCache.maxSize() != maxSize) {
            _lruCache.resize(maxSize)
        }
    }

    override fun init(
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
        store.keys().forEach { key ->
            val size = sizeOfEntry(key, null)
            _lruCache.put(key, size)
        }
    }

    override fun putCache(key: String, value: ByteArray) {
        store.putCache(key, value).also {
            val size = sizeOfEntry(key, value)
            _lruCache.put(key, size)
        }
    }

    override fun getCache(key: String): ByteArray? {
        try {
            return store.getCache(key)
        } finally {
            _lruCache.get(key)
        }
    }

    override fun removeCache(key: String) {
        try {
            store.removeCache(key)
        } finally {
            _lruCache.remove(key)
        }
    }

    protected abstract fun sizeOfEntry(key: String, value: ByteArray?): Int
}