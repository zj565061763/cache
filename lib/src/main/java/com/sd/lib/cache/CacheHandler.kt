package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

internal interface CacheHandler<T> {
    fun putCache(key: String, value: T, clazz: Class<T>): Boolean
    fun getCache(key: String, clazz: Class<T>): T?
    fun removeCache(key: String)
    fun containsCache(key: String): Boolean
    fun keys(transform: (List<String>) -> List<String>): List<String>
}

internal fun <T> newCacheHandler(cacheInfo: CacheInfo): CacheHandler<T> {
    return CacheHandlerImpl(cacheInfo)
}

internal interface CacheInfo {
    val cacheStore: CacheStore
    val objectConverter: Cache.ObjectConverter
}

private class CacheHandlerImpl<T>(
    private val cacheInfo: CacheInfo,
) : CacheHandler<T> {

    override fun putCache(key: String, value: T, clazz: Class<T>): Boolean {
        return runCatching {
            val data = encode(value, clazz)
            synchronized(CacheLock) {
                getCacheStore().putCache(key, data)
            }
        }.getOrElse {
            libNotifyException(it)
            false
        }
    }

    override fun getCache(key: String, clazz: Class<T>): T? {
        return runCatching {
            synchronized(CacheLock) {
                getCacheStore().getCache(key)
            }?.let { data ->
                decode(data, clazz)
            }
        }.getOrElse {
            libNotifyException(it)
            null
        }
    }

    override fun removeCache(key: String) {
        runCatching {
            synchronized(CacheLock) {
                getCacheStore().removeCache(key)
            }
        }.onFailure {
            libNotifyException(it)
        }
    }

    override fun containsCache(key: String): Boolean {
        return runCatching {
            synchronized(CacheLock) {
                getCacheStore().containsCache(key)
            }
        }.getOrElse {
            libNotifyException(it)
            false
        }
    }

    override fun keys(transform: (List<String>) -> List<String>): List<String> {
        return runCatching {
            synchronized(CacheLock) {
                val keys = getCacheStore().keys()
                transform(keys)
            }
        }.getOrElse {
            libNotifyException(it)
            emptyList()
        }
    }

    private fun getCacheStore(): CacheStore = cacheInfo.cacheStore

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