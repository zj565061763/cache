package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

internal class CacheImpl<T>(
  private val clazz: Class<T>,
  private val cacheStoreProvider: () -> CacheStore,
) : Cache<T> {

  var cacheChangeCallback: CacheStore.CacheChangeCallback? = null

  override fun put(key: String, value: T?): Boolean {
    if (value == null) return false
    return libRunCatching {
      val data = encode(value, clazz)
      multiProcessLock {
        getCacheStore().putCache(key, data)
      }
      true
    }.getOrElse { false }
  }

  override fun get(key: String): T? {
    return libRunCatching {
      multiProcessLock {
        getCacheStore().getCache(key)
      }?.let { data ->
        decode(data, clazz)
      }
    }.getOrNull()
  }

  override fun remove(key: String) {
    libRunCatching {
      multiProcessLock {
        getCacheStore().removeCache(key)
      }
    }
  }

  override fun contains(key: String): Boolean {
    return libRunCatching {
      multiProcessLock {
        getCacheStore().containsCache(key)
      }
    }.getOrElse { false }
  }

  override fun keys(): List<String> {
    return libRunCatching {
      multiProcessLock {
        getCacheStore().keys()
      }
    }.getOrElse { emptyList() }
  }

  private fun getCacheStore(): CacheStore {
    return cacheStoreProvider().also { cacheStore ->
      cacheStore.setCacheChangeCallback(_cacheChangeCallback)
    }
  }

  private fun encode(value: T, clazz: Class<T>): ByteArray {
    return getObjectConverter().encode(value, clazz).also { bytes ->
      if (bytes.isEmpty()) {
        libException("ObjectConverter.encode returns empty ${clazz.name}")
      }
    }
  }

  private fun decode(bytes: ByteArray, clazz: Class<T>): T? {
    if (bytes.isEmpty()) return null
    return getObjectConverter().decode(bytes, clazz)
  }

  private fun getObjectConverter(): CacheConfig.ObjectConverter = CacheConfig.get().objectConverter

  private val _cacheChangeCallback = object : CacheStore.CacheChangeCallback {
    override fun onCreate(key: String) {
      cacheChangeCallback?.onCreate(key)
    }

    override fun onModify(key: String) {
      cacheChangeCallback?.onModify(key)
    }

    override fun onRemove(key: String) {
      cacheChangeCallback?.onRemove(key)
    }
  }

  init {
    getCacheStore()
  }
}