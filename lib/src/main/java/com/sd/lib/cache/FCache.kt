package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import java.util.concurrent.ConcurrentHashMap

object FCache {
  /** 缓存所有的[Cache] */
  private val _caches = mutableMapOf<Class<*>, Cache<*>>()
  private val _mapGroupCacheStoreFactory = mutableMapOf<String, GroupCacheStoreFactory>()

  /** 获取[clazz]对应的[Cache] */
  @JvmStatic
  fun <T> get(clazz: Class<T>): Cache<T> {
    return synchronized(_caches) {
      val cache = _caches.getOrPut(clazz) { newCache(clazz) }
      @Suppress("UNCHECKED_CAST")
      cache as Cache<T>
    }
  }

  private fun <T> newCache(clazz: Class<T>): Cache<T> {
    val annotation = clazz.getAnnotation(CacheEntity::class.java)
    require(annotation != null) { "Annotation ${CacheEntity::class.java.simpleName} was not found in $clazz" }

    val id = annotation.id
    require(id.isNotBlank()) { "${CacheEntity::class.java.simpleName}.id is blank in $clazz" }

    val group = annotation.group
    require(group.isNotBlank()) { "${CacheEntity::class.java.simpleName}.group is blank in $clazz" }

    val groupCacheStoreFactory = _mapGroupCacheStoreFactory.getOrPut(group) { GroupCacheStoreFactory(group) }

    val lock = when (annotation.lockLevel) {
      CacheLockLevel.CurrentProcessCurrentCache -> Any()
      CacheLockLevel.CurrentProcessCurrentGroup -> groupCacheStoreFactory
      CacheLockLevel.CurrentProcess -> CurrentProcessLock
    }

    return CacheImpl(
      clazz = clazz,
      lock = lock,
      cacheStoreProvider = { groupCacheStoreFactory.create(id = id, clazz = clazz) },
    )
  }
}

object FCacheKtx {
  private val _caches = mutableMapOf<Class<*>, CacheKtx<*>>()

  fun <T> get(clazz: Class<T>): CacheKtx<T> {
    return synchronized(_caches) {
      val cache = _caches.getOrPut(clazz) { CacheKtxImpl(FCache.get(clazz) as CacheImpl<T>) }
      @Suppress("UNCHECKED_CAST")
      cache as CacheKtx<T>
    }
  }
}

/** 当前进程锁 */
private val CurrentProcessLock = Any()

private class GroupCacheStoreFactory(
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