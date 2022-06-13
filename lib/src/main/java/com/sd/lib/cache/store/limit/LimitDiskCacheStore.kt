package com.sd.lib.cache.store.limit

import android.util.Log
import android.util.LruCache
import com.sd.lib.cache.Cache
import com.sd.lib.cache.store.SimpleDiskCacheStore
import java.io.File

/**
 * 限制的磁盘缓存
 */
abstract class LimitDiskCacheStore(maxSize: Int, directory: File) : SimpleDiskCacheStore(directory) {
    private val _tag = javaClass.simpleName

    private val _lruCache = object : LruCache<String, File>(maxSize) {
        override fun sizeOf(key: String?, value: File?): Int {
            return sizeOfCache(value!!)
        }

        override fun entryRemoved(evicted: Boolean, key: String?, oldValue: File?, newValue: File?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            if (evicted) {
                Log.i(_tag, "evicted count:${size()}")
                synchronized(Cache::class.java) {
                    try {
                        oldValue?.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean {
        return super.putCacheImpl(key, value, file).also {
            if (it) {
                _lruCache.put(file.name, file)
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
        synchronized(Cache::class.java) {
            getCacheFiles().forEach { file ->
                _lruCache.put(file.name, file)
            }
        }
    }

    protected abstract fun sizeOfCache(file: File): Int

    init {
        checkLimit()
    }
}