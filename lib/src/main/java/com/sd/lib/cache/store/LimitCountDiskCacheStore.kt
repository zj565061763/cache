package com.sd.lib.cache.store

import android.util.Log
import android.util.LruCache
import com.sd.lib.cache.Cache
import java.io.File

/**
 * 限制数量的磁盘缓存
 */
class LimitCountDiskCacheStore(count: Int, directory: File) : SimpleDiskCacheStore(directory) {
    private val _tag = LimitCountDiskCacheStore::class.java.simpleName

    private val _lruCache = object : LruCache<String, String>(count) {
        override fun entryRemoved(evicted: Boolean, key: String?, oldValue: String?, newValue: String?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            if (evicted) {
                Log.i(_tag, "evicted cache count:${size()}")
                synchronized(Cache::class.java) {
                    removeCache(key!!)
                }
            }
        }
    }

    override fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean {
        return super.putCacheImpl(key, value, file).also {
            if (it) {
                _lruCache.put(key, "")
                Log.i(_tag, "put cache count:${_lruCache.size()}")
            }
        }
    }

    override fun removeCacheImpl(key: String, file: File): Boolean {
        return super.removeCacheImpl(key, file).also {
            if (it) {
                _lruCache.remove(key)
                Log.i(_tag, "remove cache count:${_lruCache.size()}")
            }
        }
    }

    init {
        getKeys().forEach { key ->
            _lruCache.put(key, "")
        }
    }
}