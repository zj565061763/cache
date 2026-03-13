package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

internal class GroupCacheStoreFactory(
  val group: String,
) {
  private var _isClosed = false
  private val _stores = mutableMapOf<String, StoreInfo>()

  @Throws(Throwable::class)
  fun create(id: String, clazz: Class<*>): CacheStore {
    if (_isClosed) libError("Group:${group} Closed")

    _stores[id]?.also { info ->
      if (info.clazz == clazz) {
        return info.cacheStore
      } else {
        libError("id:${id} has bound to ${info.clazz.name} when bind ${clazz.name}")
      }
    }

    return CacheConfig.get().newCacheStore(group = group, id = id).also {
      _stores[id] = StoreInfo(clazz, it)
    }
  }

  fun close() {
    _isClosed = true
    while (_stores.isNotEmpty()) {
      _stores.keys.toTypedArray().forEach { key ->
        _stores.remove(key)?.cacheStore?.also { libRunCatching { it.close() } }
      }
    }
  }

  private class StoreInfo(
    val clazz: Class<*>,
    val cacheStore: CacheStore,
  )
}