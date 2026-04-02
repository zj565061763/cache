package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import java.util.concurrent.ConcurrentHashMap

internal class GroupCacheStoreFactory(
  val group: String,
) {
  private val _stores: MutableMap<String, StoreInfo> = ConcurrentHashMap()

  @Throws(Throwable::class)
  fun create(id: String, clazz: Class<*>): CacheStore {
    _stores[id]?.also { info ->
      if (info.clazz == clazz) {
        return info.cacheStore
      } else {
        libError("id:${id} has bound to ${info.clazz.name} when bind ${clazz.name}")
      }
    }
    synchronized(this@GroupCacheStoreFactory) {
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
  }

  private class StoreInfo(
    val clazz: Class<*>,
    val cacheStore: CacheStore,
  )
}