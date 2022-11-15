package com.sd.lib.cache.handler

import com.sd.lib.cache.Cache
import com.sd.lib.cache.Cache.CacheStore
import com.sd.lib.cache.Cache.CommonCache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.exception.CacheException

/**
 * 缓存处理基类
 */
internal abstract class BaseCacheHandler<T>(
    val cacheInfo: CacheInfo,
    val keyPrefix: String,
) : CacheHandler<T>, CommonCache<T> {

    private val _cacheStore: CacheStore
        get() = cacheInfo.cacheStore

    init {
        require(keyPrefix.isNotEmpty()) { "keyPrefix is empty" }
    }

    //---------- CommonCache start ----------

    final override fun put(key: String, value: T?): Boolean {
        return putCache(key, value)
    }

    final override fun get(key: String, defaultValue: T): T {
        return getCache(key, null) ?: defaultValue
    }

    final override fun remove(key: String): Boolean {
        return removeCache(key)
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
        cacheInfo.exceptionHandler.onException(e)
    }

    //---------- CacheHandler start ----------

    final override fun putCache(key: String, value: T?): Boolean {
        if (value == null) return false
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                val data = encodeToByteImpl(value)
                _cacheStore.putCache(key, data)
            } catch (e: Exception) {
                notifyException(e)
                false
            }
        }
    }

    final override fun getCache(key: String, clazz: Class<*>?): T? {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                _cacheStore.getCache(key)?.let { data ->
                    decodeFromByte(key, data, clazz)
                }
            } catch (e: Exception) {
                notifyException(e)
                null
            }
        }
    }

    final override fun removeCache(key: String): Boolean {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                _cacheStore.removeCache(key)
            } catch (e: Exception) {
                notifyException(e)
                false
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

    @Throws(Exception::class)
    private fun decodeFromByte(key: String, data: ByteArray, clazz: Class<*>?): T {
        check(data.isNotEmpty()) { "Data is empty. key:$key" }
        return decodeFromByteImpl(data, clazz)
    }

    /**
     * 缓存转byte
     */
    @Throws(Exception::class)
    protected abstract fun encodeToByteImpl(value: T): ByteArray

    /**
     * byte转缓存
     */
    @Throws(Exception::class)
    protected abstract fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): T
}