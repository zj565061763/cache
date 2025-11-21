package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

internal fun interface CacheStoreOwner {
  fun getCacheStore(): CacheStore
}

internal class CacheImpl<T>(
  private val clazz: Class<T>,
  private val cacheStoreOwner: CacheStoreOwner,
) : Cache<T> {

  var onChange: ((key: String, data: T?) -> Unit)? = null

  override fun put(key: String, value: T?): Boolean {
    if (value == null) return false
    return libRunCatching {
      val data = encode(value, clazz)
      cacheLock {
        getCacheStore().putCache(key, data)
        onChange?.invoke(key, value)
        true
      }
    }.getOrElse { false }
  }

  override fun get(key: String): T? {
    return libRunCatching {
      cacheLock {
        getCacheStore().getCache(key)
      }?.let { data ->
        decode(data, clazz)
      }
    }.getOrNull()
  }

  override fun remove(key: String) {
    libRunCatching {
      cacheLock {
        getCacheStore().removeCache(key)
        onChange?.invoke(key, null)
      }
    }
  }

  override fun contains(key: String): Boolean {
    return libRunCatching {
      cacheLock {
        getCacheStore().containsCache(key)
      }
    }.getOrElse { false }
  }

  override fun keys(): List<String> {
    return libRunCatching {
      cacheLock {
        getCacheStore().keys()
      }
    }.getOrElse { emptyList() }
  }

  private fun encode(value: T, clazz: Class<T>): ByteArray {
    return getObjectConverter().encode(value, clazz).also {
      if (it.isEmpty()) {
        libException("Converter encode returns empty ${clazz.name}")
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