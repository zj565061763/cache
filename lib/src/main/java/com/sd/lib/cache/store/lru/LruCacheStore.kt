package com.sd.lib.cache.store.lru

import android.util.Log
import android.util.LruCache
import com.sd.lib.cache.Cache
import com.sd.lib.mutator.FMutator
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Lru算法的缓存
 */
abstract class LruCacheStore(limit: Int) : Cache.CacheStore {
    private val _tag = javaClass.simpleName
    @Volatile
    private var _activeKeyHolder: MutableMap<String, String>? = ConcurrentHashMap()

    private val _scope = MainScope()
    private val _mutator = FMutator()

    private val _lruCache = object : LruCache<String, Int>(limit) {
        override fun sizeOf(key: String, value: Int): Int {
            /**
             * 注意，[sizeOf]方法调用时已经被[_lruCache]锁住，所以不要再去锁[Cache]了，可能会造成死锁
             */
            return sizeOfLruCacheEntry(key, value)
        }

        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Int?, newValue: Int?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            if (evicted) {
                logMsg("----- $key count:${size()}")
                synchronized(Cache::class.java) {
                    try {
                        onLruCacheEntryEvicted(key)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        logMsg("evicted error:$e")
                    }
                }
            }
        }
    }

    private fun checkInit() {
        if (_activeKeyHolder == null) return
        _scope.launch {
            try {
                _mutator.mutate {
                    withContext(Dispatchers.IO) {
                        val activeKeyHolder = _activeKeyHolder ?: return@withContext
                        val map = getLruCacheMap() ?: mapOf()
                        logMsg("checkInit start count:${_lruCache.size()} cacheSize:${map.size}")

                        for ((key, value) in map) {
                            if (activeKeyHolder.containsKey(key)) continue

                            val size = _lruCache.size()
                            _lruCache.put(key, value)
                            logMsg("checkInit +++++ $key ($size -> ${_lruCache.size()})")

                            yield()
                        }

                        _activeKeyHolder = null
                        logMsg("checkInit end count:${_lruCache.size()}")
                    }
                }
            } catch (e: Exception) {
                Log.i(_tag, "checkInit error:${e}")
                throw e
            }
        }
    }

    final override fun putCache(key: String, value: ByteArray): Boolean {
        return putCacheImpl(key, value).also {
            if (it) {
                val key = transformKeyForLruCache(key)
                _activeKeyHolder?.put(key, "")

                val size = _lruCache.size()
                _lruCache.put(key, value.size)
                logMsg("+++++ $key ($size -> ${_lruCache.size()})")

                checkInit()
            }
        }
    }

    final override fun getCache(key: String): ByteArray? {
        return getCacheImpl(key)
    }

    final override fun removeCache(key: String): Boolean {
        return removeCacheImpl(key).also {
            if (it) {
                val key = transformKeyForLruCache(key)
                _lruCache.remove(key)
            }
        }
    }

    final override fun containsCache(key: String): Boolean {
        return containsCacheImpl(key)
    }

    // -------------------- Basic start --------------------

    protected abstract fun putCacheImpl(key: String, value: ByteArray): Boolean

    protected abstract fun getCacheImpl(key: String): ByteArray?

    protected abstract fun removeCacheImpl(key: String): Boolean

    protected abstract fun containsCacheImpl(key: String): Boolean

    // -------------------- Basic end --------------------

    /**
     * 返回所有key和每个key对应的字节数量，
     * 如果子类重写了[transformKeyForLruCache]对key进行转换，此处要返回转换后的key
     */
    protected abstract fun getLruCacheMap(): Map<String, Int>?

    /**
     * 返回缓存项的大小，[byteCount]为该缓存项的字节数量(单位B)
     */
    protected abstract fun sizeOfLruCacheEntry(key: String, byteCount: Int): Int

    /**
     * 缓存被驱逐回调，子类需要移除[key]对应的缓存，
     * 如果子类重写了[transformKeyForLruCache]对key进行转换，则参数[key]是转换后的key，
     * 此方法已经同步锁住[Cache]
     */
    @Throws(Exception::class)
    protected abstract fun onLruCacheEntryEvicted(key: String)

    /**
     * 如果子类在保存缓存的时候对key进行了转换，需要重写此方法转换LruCache的key，
     */
    @Throws(Exception::class)
    protected open fun transformKeyForLruCache(key: String): String {
        return key
    }

    protected open fun logMsg(msg: String) {
        Log.i(_tag, msg)
    }
}