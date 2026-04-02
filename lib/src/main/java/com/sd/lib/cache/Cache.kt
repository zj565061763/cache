package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

interface Cache<T> {
  /** 设置缓存 */
  fun put(key: String, value: T?): Boolean

  /** 获取缓存 */
  fun get(key: String): T?

  /** 删除缓存 */
  fun remove(key: String): Boolean

  /** 所有缓存key */
  fun keys(): List<String>
}

internal class CacheImpl<T>(
  private val clazz: Class<T>,
  val lock: Any,
  private val cacheStoreProvider: () -> CacheStore,
) : Cache<T> {
  @Volatile
  var cacheChangeCallback: CacheStore.CacheChangeCallback? = null

  override fun put(key: String, value: T?): Boolean {
    if (value == null) return false
    return libRunCatching {
      val data = encode(value, clazz)
      lockCache { getCacheStore().putCache(key, data) }
      true
    }.getOrElse { false }
  }

  override fun get(key: String): T? {
    return libRunCatching {
      lockCache { getCacheStore().getCache(key) }
        ?.let { data -> decode(data, clazz) }
    }.getOrNull()
  }

  override fun remove(key: String): Boolean {
    return libRunCatching {
      lockCache { getCacheStore().removeCache(key) }
    }.getOrElse { false }
  }

  override fun keys(): List<String> {
    return libRunCatching {
      lockCache { getCacheStore().keys() }
    }.getOrElse { emptyList() }
  }

  @Throws(Throwable::class)
  private fun getCacheStore(): CacheStore {
    return cacheStoreProvider().also { cacheStore ->
      cacheStore.setCacheChangeCallback(_cacheChangeCallback)
    }
  }

  @Throws(Throwable::class)
  private fun encode(value: T, clazz: Class<T>): ByteArray {
    return getObjectConverter().encode(value, clazz).also { bytes ->
      if (bytes.isEmpty()) {
        libException("ObjectConverter.encode returns empty ${clazz.name}")
      }
    }
  }

  @Throws(Throwable::class)
  private fun decode(bytes: ByteArray, clazz: Class<T>): T? {
    if (bytes.isEmpty()) return null
    return getObjectConverter().decode(bytes, clazz)
  }

  private fun getObjectConverter(): CacheConfig.ObjectConverter = CacheConfig.get().objectConverter

  private val _cacheChangeCallback = object : CacheStore.CacheChangeCallback {
    override fun onRemove(key: String) {
      cacheChangeCallback?.onRemove(key)
    }

    override fun onModify(key: String) {
      cacheChangeCallback?.onModify(key)
    }
  }
}