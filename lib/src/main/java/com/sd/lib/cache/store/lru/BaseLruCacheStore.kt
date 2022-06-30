package com.sd.lib.cache.store.lru

import android.util.Log
import android.util.LruCache
import com.sd.lib.cache.Cache
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * Lru算法的缓存
 */
abstract class BaseLruCacheStore(limit: Int) : Cache.CacheStore {
    @Volatile
    private var _activeKeyHolder: MutableMap<String, String>? = ConcurrentHashMap()
    @Volatile
    private var _initThread: Thread? = null

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
                logMsg("--- $key count:${size()} ${Thread.currentThread().name}")
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
        if (_activeKeyHolder == null) {
            // 已经初始化过了
            return
        }
        if (_initThread != null) {
            // 正在初始化
            return
        }

        /** 这里仅同步[_initThread]，不同步[initLruCache] */
        synchronized(this@BaseLruCacheStore) {
            if (_initThread != null) return
            _initThread = thread {
                initLruCache()
            }
        }
    }

    private fun initLruCache() {
        val activeKeyHolder = _activeKeyHolder ?: return
        val map = try {
            getLruCacheSizeMap() ?: mapOf()
        } catch (e: Exception) {
            e.printStackTrace()
            logMsg("initLruCache getLruCacheSizeMap error:$e")
            return
        }

        logMsg("initLruCache start count:${_lruCache.size()} cacheSize:${map.size}")

        for ((key, value) in map) {
            if (activeKeyHolder.containsKey(key)) continue

            logMsg("initLruCache +++ start $key ${_lruCache.size()}")
            _lruCache.put(key, value)
            logMsg("initLruCache +++++++++++++++ end $key ${_lruCache.size()}")
        }

        // 初始化结束，重置
        _activeKeyHolder = null
        _initThread = null
        logMsg("initLruCache end count:${_lruCache.size()}")
    }

    final override fun putCache(key: String, value: ByteArray): Boolean {
        return putCacheImpl(key, value).also {
            if (it) {
                val key = transformKeyForLruCache(key)
                _activeKeyHolder?.put(key, "")

                logMsg("+++ start $key ${_lruCache.size()}")
                _lruCache.put(key, value.size)
                logMsg("+++++++++++++++ end $key ${_lruCache.size()}")

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
    @Throws(Exception::class)
    protected abstract fun getLruCacheSizeMap(): Map<String, Int>?

    /**
     * 返回缓存项的大小，[byteCount]为该缓存项的字节数量(单位B)
     */
    protected abstract fun sizeOfLruCacheEntry(key: String, byteCount: Int): Int

    /**
     * 缓存被驱逐回调，子类需要移除[key]对应的缓存，
     * 如果子类重写了[transformKeyForLruCache]对key进行转换，则参数[key]是转换后的key，
     * 此方法有可能在子线程执行，已经同步锁住[Cache]
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
        Log.i(javaClass.simpleName, msg)
    }
}