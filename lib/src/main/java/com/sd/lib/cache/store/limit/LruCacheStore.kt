package com.sd.lib.cache.store.limit

import android.util.Log
import android.util.LruCache
import com.sd.lib.cache.Cache
import kotlin.concurrent.thread

abstract class LruCacheStore(maxSize: Int) : Cache.CacheStore {
    private val _tag = javaClass.simpleName
    private var _hasCheckLimit = false

    private val _lruCache = object : LruCache<String, String>(maxSize) {
        override fun sizeOf(key: String?, value: String?): Int {
            return onLruCacheSizeOfCache(key!!)
        }

        override fun entryRemoved(evicted: Boolean, key: String?, oldValue: String?, newValue: String?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            if (evicted) {
                Log.i(_tag, "evicted count:${size()}")
                onLruCacheCacheEvicted(key!!)
            }
        }
    }

    private fun checkLimit() {
        if (_hasCheckLimit) return
        synchronized(this@LruCacheStore) {
            if (_hasCheckLimit) return
            _hasCheckLimit = true
        }

        thread {
            onLruCacheInitKeys().forEach { key ->
                _lruCache.put(key, "")
            }
            Log.i(_tag, "checkLimit count:${_lruCache.size()}")
        }
    }

    final override fun putCache(key: String, value: ByteArray): Boolean {
        return putCacheImpl(key, value).also {
            if (it) {
                _lruCache.put(key, "")
                checkLimit()
                Log.i(_tag, "put count:${_lruCache.size()}")
            }
        }
    }

    final override fun getCache(key: String): ByteArray? {
        return getCacheImpl(key)
    }

    final override fun removeCache(key: String): Boolean {
        return removeCacheImpl(key).also {
            if (it) {
                _lruCache.remove(key)
                Log.i(_tag, "remove count:${_lruCache.size()}")
            }
        }
    }

    final override fun containsCache(key: String): Boolean {
        return containsCacheImpl(key)
    }

    protected abstract fun putCacheImpl(key: String, value: ByteArray): Boolean

    protected abstract fun getCacheImpl(key: String): ByteArray?

    protected abstract fun removeCacheImpl(key: String): Boolean

    protected abstract fun containsCacheImpl(key: String): Boolean

    /**
     * 返回所有LruCache缓存的key
     */
    protected abstract fun onLruCacheInitKeys(): List<String>

    /**
     * 返回[key]对应的LruCache缓存大小
     */
    protected abstract fun onLruCacheSizeOfCache(key: String): Int

    /**
     * LruCache缓存被驱逐，子类需要移除[key]对应的缓存
     */
    protected abstract fun onLruCacheCacheEvicted(key: String)
}