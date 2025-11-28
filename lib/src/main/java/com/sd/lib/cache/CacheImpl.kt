package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

internal class CacheImpl<T>(
  private val clazz: Class<T>,
  private val getCacheStore: () -> CacheStore,
) : Cache<T> {

  var onChange: ((key: String) -> Unit)? = null

  override fun put(key: String, value: T?): Boolean {
    if (value == null) return false
    return libRunCatching {
      val data = encode(value, clazz)
      multiProcessLock {
        getCacheStoreInternal().putCache(key, data)
      }
      true
    }.getOrElse { false }
  }

  override fun get(key: String): T? {
    return libRunCatching {
      multiProcessLock {
        getCacheStoreInternal().getCache(key)
      }?.let { data ->
        decode(data, clazz)
      }
    }.getOrNull()
  }

  override fun remove(key: String) {
    libRunCatching {
      multiProcessLock {
        getCacheStoreInternal().removeCache(key)
      }
    }
  }

  override fun contains(key: String): Boolean {
    return libRunCatching {
      multiProcessLock {
        getCacheStoreInternal().containsCache(key)
      }
    }.getOrElse { false }
  }

  override fun keys(): List<String> {
    return libRunCatching {
      multiProcessLock {
        getCacheStoreInternal().keys()
      }
    }.getOrElse { emptyList() }
  }

  private fun getCacheStoreInternal(): CacheStore {
    return getCacheStore().also { cacheStore ->
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
      onChange?.invoke(key)
    }

    override fun onModify(key: String) {
      onChange?.invoke(key)
    }

    override fun onRemove(key: String) {
      onChange?.invoke(key)
    }
  }

  init {
    getCacheStoreInternal()
  }
}