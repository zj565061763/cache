package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

internal class CacheImpl<T>(
  private val clazz: Class<T>,
  private val getCacheStore: () -> CacheStore,
) : Cache<T> {

  var onChange: ((key: String, data: T?) -> Unit)? = null

  override fun put(key: String, value: T?): Boolean {
    if (value == null) return false
    return libRunCatching {
      val data = encode(value, clazz)
      getCacheStore().putCache(key, data)
      onChange?.invoke(key, value)
      true
    }.getOrElse { false }
  }

  override fun get(key: String): T? {
    return libRunCatching {
      getCacheStore().getCache(key)?.let { data ->
        decode(data, clazz)
      }
    }.getOrNull()
  }

  override fun remove(key: String) {
    libRunCatching {
      getCacheStore().removeCache(key)
      onChange?.invoke(key, null)
    }
  }

  override fun contains(key: String): Boolean {
    return libRunCatching {
      getCacheStore().containsCache(key)
    }.getOrElse { false }
  }

  override fun keys(): List<String> {
    return libRunCatching {
      getCacheStore().keys()
    }.getOrElse { emptyList() }
  }

  private fun encode(value: T, clazz: Class<T>): ByteArray {
    return getObjectConverter().encode(value, clazz).also {
      if (it.isEmpty()) {
        libException("ObjectConverter.encode returns empty ${clazz.name}")
      }
    }
  }

  private fun decode(bytes: ByteArray, clazz: Class<T>): T? {
    if (bytes.isEmpty()) return null
    return getObjectConverter().decode(bytes, clazz)
  }

  private fun getObjectConverter(): CacheConfig.ObjectConverter = CacheConfig.get().objectConverter
}