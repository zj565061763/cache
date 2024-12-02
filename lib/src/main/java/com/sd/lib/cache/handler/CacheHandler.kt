package com.sd.lib.cache.handler

import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheLock
import com.sd.lib.cache.libError
import com.sd.lib.cache.store.CacheStore

/**
 * 缓存处理接口
 */
internal interface CacheHandler<T> {
    fun putCache(key: String, value: T, clazz: Class<T>): Boolean
    fun getCache(key: String, clazz: Class<T>): T?
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

/**
 * 缓存处理基类
 */
internal abstract class BaseCacheHandler<T>(
    private val cacheInfo: CacheInfo,
    handlerKey: String,
) : CacheHandler<T> {

    private val _keyPrefix = "${handlerKey}_"

    private val _cacheStore: CacheStore
        get() = cacheInfo.cacheStore

    init {
        if (handlerKey.isEmpty()) libError("handlerKey is empty")
    }

    private fun packKey(key: String): String {
        if (key.isEmpty()) libError("key is empty")
        return _keyPrefix + key
    }

    private fun unpackKey(key: String): String {
        if (key.isEmpty()) libError("key is empty")
        return key.removePrefix(_keyPrefix)
    }

    private fun notifyException(error: Throwable) {
        cacheInfo.exceptionHandler.onException(error)
    }

    //---------- CacheHandler start ----------

    @Suppress("NAME_SHADOWING")
    final override fun putCache(key: String, value: T, clazz: Class<T>): Boolean {
        val key = packKey(key)
        return runCatching {
            val data = encode(value, clazz, cacheInfo.objectConverter)
            synchronized(CacheLock) {
                _cacheStore.putCache(key, data)
            }
        }.getOrElse {
            notifyException(it)
            false
        }
    }

    @Suppress("NAME_SHADOWING")
    final override fun getCache(key: String, clazz: Class<T>): T? {
        val key = packKey(key)
        return runCatching {
            synchronized(CacheLock) {
                _cacheStore.getCache(key)
            }?.let { data ->
                decode(data, clazz, cacheInfo.objectConverter)
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
        synchronized(CacheLock) {
            return _cacheStore.keys().asSequence()
                .filter { it.startsWith(_keyPrefix) }
                .map { unpackKey(it) }
                .map(transform)
                .toList()
        }
    }

    //---------- CacheHandler end ----------

    protected abstract fun encode(value: T, clazz: Class<T>, converter: Cache.ObjectConverter): ByteArray
    protected abstract fun decode(bytes: ByteArray, clazz: Class<T>, converter: Cache.ObjectConverter): T?
}