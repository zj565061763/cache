package com.sd.lib.cache.handler

import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheLock
import com.sd.lib.cache.notifyException
import com.sd.lib.cache.store.CacheStore

/**
 * 缓存处理接口
 */
internal interface CacheHandler<T> {
    fun putCache(key: String, value: T, clazz: Class<T>?): Boolean

    fun getCache(key: String, clazz: Class<T>?): T?

    fun removeCache(key: String)

    fun containsCache(key: String): Boolean

    fun keys(transform: (String) -> String): List<String>
}

internal interface CacheInfo {
    /** 仓库 */
    val cacheStore: CacheStore

    /** 对象转换 */
    val objectConverter: Cache.ObjectConverter

    /** 异常处理 */
    val exceptionHandler: Cache.ExceptionHandler
}

private const val KeyPrefixTag = "f_cache_"

/**
 * 缓存处理基类
 */
internal abstract class BaseCacheHandler<T>(
    val cacheInfo: CacheInfo,
    handlerKey: String,
) : CacheHandler<T>, Cache.CommonCache<T> {

    private val _keyPrefix = KeyPrefixTag + "${handlerKey}_"

    private val _cacheStore: CacheStore
        get() = cacheInfo.cacheStore

    init {
        require(handlerKey.isNotEmpty()) { "handlerKey is empty" }
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

    private fun packKey(key: String): String {
        require(key.isNotEmpty()) { "key is empty" }
        return _keyPrefix + key
    }

    private fun unpackKey(key: String): String {
        require(key.isNotEmpty()) { "key is empty" }
        return key.removePrefix(_keyPrefix)
    }

    private fun notifyException(error: Throwable) {
        cacheInfo.exceptionHandler.notifyException(error)
    }

    //---------- CacheHandler start ----------

    final override fun putCache(key: String, value: T, clazz: Class<T>?): Boolean {
        @Suppress("NAME_SHADOWING")
        val key = packKey(key)
        return kotlin.runCatching {
            val data = encode(value, clazz)
            synchronized(CacheLock) {
                _cacheStore.putCache(key, data)
            }
        }.getOrElse {
            notifyException(it)
            false
        }
    }

    final override fun getCache(key: String, clazz: Class<T>?): T? {
        @Suppress("NAME_SHADOWING")
        val key = packKey(key)
        return kotlin.runCatching {
            synchronized(CacheLock) {
                _cacheStore.getCache(key)
            }?.let { data ->
                decode(data, clazz)
            }
        }.getOrElse {
            notifyException(it)
            null
        }
    }

    final override fun removeCache(key: String) {
        @Suppress("NAME_SHADOWING")
        val key = packKey(key)
        kotlin.runCatching {
            synchronized(CacheLock) {
                _cacheStore.removeCache(key)
            }
        }.onFailure {
            notifyException(it)
        }
    }

    final override fun containsCache(key: String): Boolean {
        @Suppress("NAME_SHADOWING")
        val key = packKey(key)
        return kotlin.runCatching {
            synchronized(CacheLock) {
                _cacheStore.containsCache(key)
            }
        }.getOrElse {
            notifyException(it)
            false
        }
    }

    final override fun keys(transform: (String) -> String): List<String> {
        return kotlin.runCatching {
            synchronized(CacheLock) {
                val keys = _cacheStore.keys()
                if (keys.isNullOrEmpty()) {
                    emptyList()
                } else {
                    keys.asSequence()
                        .filter { it.startsWith(_keyPrefix) }
                        .map { unpackKey(it) }
                        .map(transform)
                        .toList()
                }
            }
        }.getOrElse {
            notifyException(it)
            emptyList()
        }
    }

    //---------- CacheHandler end ----------

    /**
     * 编码
     */
    protected abstract fun encode(value: T, clazz: Class<T>?): ByteArray

    /**
     * 解码
     */
    protected abstract fun decode(bytes: ByteArray, clazz: Class<T>?): T?
}