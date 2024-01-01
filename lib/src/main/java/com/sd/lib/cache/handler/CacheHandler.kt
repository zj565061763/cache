package com.sd.lib.cache.handler

import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheException
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.store.CacheStore
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

    private val _cacheStore: CacheStore
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

    private fun notifyException(error: Throwable) {
        if (error is CacheException) throw error
        if (error is CancellationException) throw error
        cacheInfo.exceptionHandler.onException(error)
    }

    //---------- CacheHandler start ----------

    @Suppress("NAME_SHADOWING")
    final override fun putCache(key: String, value: T, clazz: Class<T>?): Boolean {
        val key = transformKey(key)
        return synchronized(Cache::class.java) {
            kotlin.runCatching {
                val data = encode(value, clazz)
                _cacheStore.putCache(key, data)
            }
        }.getOrElse {
            notifyException(it)
            false
        }
    }

    @Suppress("NAME_SHADOWING")
    final override fun getCache(key: String, clazz: Class<T>?): T? {
        val key = transformKey(key)
        return synchronized(Cache::class.java) {
            kotlin.runCatching {
                _cacheStore.getCache(key)?.let { data ->
                    decode(data, clazz)
                }
            }
        }.getOrElse {
            notifyException(it)
            null
        }
    }

    @Suppress("NAME_SHADOWING")
    final override fun removeCache(key: String) {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            kotlin.runCatching {
                _cacheStore.removeCache(key)
            }
        }.onFailure {
            notifyException(it)
        }
    }

    @Suppress("NAME_SHADOWING")
    final override fun containsCache(key: String): Boolean {
        val key = transformKey(key)
        return kotlin.runCatching {
            synchronized(Cache::class.java) {
                _cacheStore.containsCache(key)
            }
        }.getOrElse {
            notifyException(it)
            false
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