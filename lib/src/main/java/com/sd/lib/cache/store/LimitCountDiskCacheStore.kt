package com.sd.lib.cache.store

import android.util.Log
import android.util.LruCache
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
                Log.i(_tag, "evicted count:${size()}")
                removeFileKey(key!!)
            }
        }
    }

    override fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean {
        return super.putCacheImpl(key, value, file).also {
            if (it) {
                _lruCache.put(file.name, "")
                Log.i(_tag, "put count:${_lruCache.size()}")
            }
        }
    }

    override fun removeCacheImpl(key: String, file: File): Boolean {
        return super.removeCacheImpl(key, file).also {
            if (it) {
                _lruCache.remove(file.name)
                Log.i(_tag, "remove count:${_lruCache.size()}")
            }
        }
    }

    fun checkLimit() {
        getFileKeys().forEach { key ->
            _lruCache.put(key, "")
        }
    }

    init {
        checkLimit()
    }
}