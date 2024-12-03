package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

interface Cache<T> {
    fun put(key: String, value: T?): Boolean
    fun get(key: String): T?
    fun remove(key: String)
    fun contains(key: String): Boolean
    fun keys(): List<String>
}

internal fun interface CacheStoreOwner {
    fun getCacheStore(): CacheStore
}

internal class CacheImpl<T>(
    private val clazz: Class<T>,
    private val cacheStoreOwner: CacheStoreOwner,
) : Cache<T> {
    override fun put(key: String, value: T?): Boolean {
        if (value == null) return false
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

    override fun get(key: String): T? {
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

    override fun remove(key: String) {
        runCatching {
            synchronized(CacheLock) {
                getCacheStore().removeCache(key)
            }
        }.onFailure {
            libNotifyException(it)
        }
    }

    override fun contains(key: String): Boolean {
        return runCatching {
            synchronized(CacheLock) {
                getCacheStore().containsCache(key)
            }
        }.getOrElse {
            libNotifyException(it)
            false
        }
    }

    override fun keys(): List<String> {
        return runCatching {
            synchronized(CacheLock) {
                getCacheStore().keys()
            }
        }.getOrElse {
            libNotifyException(it)
            emptyList()
        }
    }

    private fun encode(value: T, clazz: Class<T>): ByteArray {
        return getObjectConverter().encode(value, clazz).also {
            if (it.isEmpty()) {
                throw CacheException("Converter encode returns empty ${clazz.name}")
            }
        }
    }

    private fun decode(bytes: ByteArray, clazz: Class<T>): T? {
        if (bytes.isEmpty()) return null
        return getObjectConverter().decode(bytes, clazz)
    }

    private fun getCacheStore(): CacheStore = cacheStoreOwner.getCacheStore()
    private fun getObjectConverter(): CacheConfig.ObjectConverter = CacheConfig.get().objectConverter
}