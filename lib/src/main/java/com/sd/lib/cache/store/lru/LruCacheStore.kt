package com.sd.lib.cache.store.lru

import android.util.Log
import android.util.LruCache
import com.sd.lib.cache.Cache
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * Lru算法的缓存
 */
abstract class LruCacheStore(maxSize: Int) : Cache.CacheStore {
    private val _tag = javaClass.simpleName
    private var _hasCheckLimit = false
    private var _mapActiveKey: MutableMap<String, String>? = ConcurrentHashMap()

    private val _lruCache = object : LruCache<String, String>(maxSize) {
        override fun sizeOf(key: String?, value: String?): Int {
            return onLruCacheSizeOfEntry(key!!)
        }

        override fun entryRemoved(evicted: Boolean, key: String?, oldValue: String?, newValue: String?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            if (evicted) {
                Log.i(_tag, "evicted count:${size()}")
                synchronized(Cache::class.java) {
                    onLruCacheEntryEvicted(key!!)
                }
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
            Log.i(_tag, "checkLimit start count:${_lruCache.size()}")
            onLruCacheInitKeys().forEach { key ->
                synchronized(Cache::class.java) {
                    if (!_mapActiveKey!!.containsKey(key)) {
                        Log.i(_tag, "checkLimit put $key")
                        _lruCache.put(key, "")
                    }
                }
            }
            _mapActiveKey = null
            Log.i(_tag, "checkLimit end count:${_lruCache.size()}")
        }
    }

    final override fun putCache(key: String, value: ByteArray): Boolean {
        return putCacheImpl(key, value).also {
            if (it) {
                val key = onLruCacheTransformKey(key)
                _mapActiveKey?.let { map ->
                    map[key] = ""
                }

                Log.i(_tag, "put count:${_lruCache.size()}")
                _lruCache.put(key, "")
                checkLimit()
            }
        }
    }

    final override fun getCache(key: String): ByteArray? {
        return getCacheImpl(key)
    }

    final override fun removeCache(key: String): Boolean {
        return removeCacheImpl(key).also {
            if (it) {
                val key = onLruCacheTransformKey(key)
                _lruCache.remove(key)
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
     * 返回所有缓存的key，用来初始化LruCache，如果子类有对key进行转换，此处应该返回转换后的key
     */
    protected abstract fun onLruCacheInitKeys(): List<String>

    /**
     * 返回[key]对应的缓存大小
     */
    protected abstract fun onLruCacheSizeOfEntry(key: String): Int

    /**
     * LruCache缓存被驱逐，子类需要移除[key]对应的缓存
     */
    protected abstract fun onLruCacheEntryEvicted(key: String)

    /**
     * 如果子类在保存缓存的时候对key进行了转换，需要重写此方法转换LruCache中的key
     */
    protected open fun onLruCacheTransformKey(key: String): String {
        return key
    }
}