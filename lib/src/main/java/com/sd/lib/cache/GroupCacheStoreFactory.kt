package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

internal class GroupCacheStoreFactory(
  val group: String,
) {
  private var _destroyed = false
  private val _stores = mutableMapOf<String, StoreInfo>()

  @Throws(Throwable::class)
  fun create(id: String, clazz: Class<*>): CacheStore {
    if (_destroyed) libError("Group:${group} has been destroyed")

    _stores[id]?.also { info ->
      if (info.clazz == clazz) {
        return info.cacheStore
      } else {
        libError("id:${id} has bound to ${info.clazz.name} when bind ${clazz.name}")
      }
    }

    return CacheConfig.get().newCacheStore(group = group, id = id)
      .also { cacheStore -> _stores[id] = StoreInfo(clazz, cacheStore) }
  }

  fun destroy() {
    _destroyed = true
    _stores.values.forEach { it.cacheStore.destroyQuietly() }
    _stores.clear()
  }

  private class StoreInfo(
    val clazz: Class<*>,
    val cacheStore: CacheStore,
  )
}

private fun CacheStore.destroyQuietly() = libRunCatching { destroy() }