package com.sd.lib.cache

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

internal fun <T> newCacheHandler(
    cacheInfo: CacheInfo,
    keyPrefix: String,
): CacheHandler<T> {
    return CacheHandlerImpl(cacheInfo, keyPrefix)
}

internal interface CacheInfo {
    /** 仓库 */
    val cacheStore: CacheStore

    /** 对象转换 */
    val objectConverter: Cache.ObjectConverter

    /** 异常处理 */
    val exceptionHandler: Cache.ExceptionHandler
}

private class CacheHandlerImpl<T>(
    private val cacheInfo: CacheInfo,
    keyPrefix: String,
) : CacheHandler<T> {

    private val _keyPrefix = "${keyPrefix}_"
    private val _cacheStore: CacheStore get() = cacheInfo.cacheStore

    init {
        if (keyPrefix.isEmpty()) libError("keyPrefix is empty")
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
    override fun putCache(key: String, value: T, clazz: Class<T>): Boolean {
        val key = packKey(key)
        return runCatching {
            val data = encode(value, clazz)
            synchronized(CacheLock) {
                _cacheStore.putCache(key, data)
            }
        }.getOrElse {
            notifyException(it)
            false
        }
    }

    @Suppress("NAME_SHADOWING")
    override fun getCache(key: String, clazz: Class<T>): T? {
        val key = packKey(key)
        return runCatching {
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

    @Suppress("NAME_SHADOWING")
    override fun removeCache(key: String) {
        val key = packKey(key)
        runCatching {
            synchronized(CacheLock) {
                _cacheStore.removeCache(key)
            }
        }.onFailure {
            notifyException(it)
        }
    }

    @Suppress("NAME_SHADOWING")
    override fun containsCache(key: String): Boolean {
        val key = packKey(key)
        return runCatching {
            synchronized(CacheLock) {
                _cacheStore.containsCache(key)
            }
        }.getOrElse {
            notifyException(it)
            false
        }
    }

    override fun keys(transform: (String) -> String): List<String> {
        synchronized(CacheLock) {
            return runCatching {
                _cacheStore.keys()
            }.getOrElse {
                notifyException(it)
                emptyList()
            }.asSequence()
                .filter { it.startsWith(_keyPrefix) }
                .map { unpackKey(it) }
                .map(transform)
                .toList()
        }
    }

    //---------- CacheHandler end ----------

    private fun encode(value: T, clazz: Class<T>): ByteArray {
        return cacheInfo.objectConverter.encode(value, clazz)
            .also {
                if (it.isEmpty()) {
                    throw CacheException("Converter encode returns empty ${clazz.name}")
                }
            }
    }

    private fun decode(bytes: ByteArray, clazz: Class<T>): T? {
        if (bytes.isEmpty()) return null
        return cacheInfo.objectConverter.decode(bytes, clazz)
    }
}