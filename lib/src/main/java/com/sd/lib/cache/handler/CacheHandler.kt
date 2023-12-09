package com.sd.lib.cache.handler

import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheException
import com.sd.lib.cache.CacheInfo
import kotlin.coroutines.cancellation.CancellationException

/**
 * 缓存处理接口
 */
internal interface CacheHandler<T> {
    fun putCache(key: String, value: T, clazz: Class<T>?): Boolean

    fun getCache(key: String, clazz: Class<T>?): T?

    fun removeCache(key: String)

    fun containsCache(key: String): Boolean
}

/**
 * 缓存处理基类
 */
internal abstract class BaseCacheHandler<T>(
    val cacheInfo: CacheInfo,
    private val keyPrefix: String,
) : CacheHandler<T>, Cache.CommonCache<T> {

    private val _cacheStore: Cache.CacheStore
        get() = cacheInfo.cacheStore

    init {
        require(keyPrefix.isNotEmpty()) { "keyPrefix is empty" }
    }

    //---------- CommonCache start ----------

    final override fun put(key: String, value: T?): Boolean {
        if (value == null) return false
        return putCache(key, value, null)
    }

    final override fun get(key: String): T? {
        return getCache(key, null)
    }

    final override fun remove(key: String) {
        removeCache(key)
    }

    final override fun contains(key: String): Boolean {
        return containsCache(key)
    }

    //---------- CommonCache end ----------

    private fun transformKey(key: String): String {
        require(key.isNotEmpty()) { "key is empty" }
        return keyPrefix + "_" + key
    }

    private fun notifyException(e: Exception) {
        if (e is CacheException) throw e
        if (e is CancellationException) throw e
        cacheInfo.exceptionHandler.onException(e)
    }

    //---------- CacheHandler start ----------

    final override fun putCache(key: String, value: T, clazz: Class<T>?): Boolean {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                val data = encode(value, clazz)
                _cacheStore.putCache(key, data)
            } catch (e: Exception) {
                notifyException(e)
                false
            }
        }
    }

    final override fun getCache(key: String, clazz: Class<T>?): T? {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                _cacheStore.getCache(key)?.let { data ->
                    decode(data, clazz)
                }
            } catch (e: Exception) {
                notifyException(e)
                null
            }
        }
    }

    final override fun removeCache(key: String) {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            try {
                _cacheStore.removeCache(key)
            } catch (e: Exception) {
                notifyException(e)
            }
        }
    }

    final override fun containsCache(key: String): Boolean {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                _cacheStore.containsCache(key)
            } catch (e: Exception) {
                notifyException(e)
                false
            }
        }
    }

    //---------- CacheHandler end ----------

    /**
     * 编码
     */
    @Throws(Exception::class)
    protected abstract fun encode(value: T, clazz: Class<T>?): ByteArray

    /**
     * 解码
     */
    @Throws(Exception::class)
    protected abstract fun decode(bytes: ByteArray, clazz: Class<T>?): T?
}