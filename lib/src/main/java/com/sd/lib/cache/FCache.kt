package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import java.util.concurrent.ConcurrentHashMap

/** 缓存入口，按实体类型获取缓存实例 */
object FCache {
  private val _mapGroupCacheStoreFactory = mutableMapOf<String, GroupCacheStoreFactory>()
  private val _cacheHolder = mutableMapOf<Class<*>, Cache<*>>()
  private val _ktxCacheHolder = mutableMapOf<Class<*>, CacheKtx<*>>()

  /** 获取[clazz]对应的[Cache] */
  @JvmStatic
  fun <T> get(clazz: Class<T>): Cache<T> {
    return synchronized(_cacheHolder) {
      val cache = _cacheHolder.getOrPut(clazz) { newCache(clazz) }
      @Suppress("UNCHECKED_CAST")
      cache as Cache<T>
    }
  }

  /** 获取[clazz]对应的[CacheKtx] */
  @JvmStatic
  fun <T> getKtx(clazz: Class<T>): CacheKtx<T> {
    return synchronized(_ktxCacheHolder) {
      val cache = _ktxCacheHolder.getOrPut(clazz) { CacheKtxImpl(get(clazz) as CacheImpl<T>) }
      @Suppress("UNCHECKED_CAST")
      cache as CacheKtx<T>
    }
  }

  private fun <T> newCache(clazz: Class<T>): Cache<T> {
    val annotation = clazz.getAnnotation(CacheEntity::class.java)
    require(annotation != null) { "Annotation ${CacheEntity::class.java.simpleName} was not found in $clazz" }

    val id = annotation.id
    require(id.isNotBlank()) { "${CacheEntity::class.java.simpleName}.id is blank in $clazz" }
    requireValidCacheEntityProperty(value = id, property = "id", clazz = clazz)

    val group = annotation.group
    require(group.isNotBlank()) { "${CacheEntity::class.java.simpleName}.group is blank in $clazz" }
    requireValidCacheEntityProperty(value = group, property = "group", clazz = clazz)

    val groupCacheStoreFactory = _mapGroupCacheStoreFactory.getOrPut(group) { GroupCacheStoreFactory(group) }

    val lock = when (annotation.lockLevel) {
      CacheLockLevel.CurrentProcessCurrentCache -> Any()
      CacheLockLevel.CurrentProcessCurrentGroup -> groupCacheStoreFactory.groupLock
      CacheLockLevel.CurrentProcess -> CurrentProcessLock
    }

    return CacheImpl(
      clazz = clazz,
      lock = lock,
      cacheStoreProvider = { groupCacheStoreFactory.create(id = id, clazz = clazz) },
    ).also {
      groupCacheStoreFactory.register(id = id, clazz = clazz)
    }
  }
}

private fun requireValidCacheEntityProperty(value: String, property: String, clazz: Class<*>) {
  try {
    value.encodeToByteArray(throwOnInvalidSequence = true)
  } catch (error: CharacterCodingException) {
    throw IllegalArgumentException(
      "${CacheEntity::class.java.simpleName}.$property contains invalid UTF-16 in $clazz",
      error,
    )
  }
}

/** 当前进程锁 */
private val CurrentProcessLock = Any()

private class GroupCacheStoreFactory(
  val group: String,
) {
  /** 当前组的缓存锁，不能与创建仓库的锁共用，否则可能死锁 */
  val groupLock = Any()

  private val _stores = ConcurrentHashMap<String, StoreInfo>()

  fun register(id: String, clazz: Class<*>) {
    _stores.putIfAbsent(id, StoreInfo(clazz = clazz))?.also { info ->
      if (info.clazz != clazz) {
        libError("id:${id} has bound to ${info.clazz.name} when bind ${clazz.name}")
      }
    }
  }

  @Throws(Throwable::class)
  fun create(id: String, clazz: Class<*>): CacheStore {
    getStoreInfo(id = id, clazz = clazz).cacheStore?.also { return it }
    synchronized(this@GroupCacheStoreFactory) {
      getStoreInfo(id = id, clazz = clazz).cacheStore?.also { return it }
      return CacheConfig.get().newCacheStore(group = group, id = id)
        .also { cacheStore -> _stores[id] = StoreInfo(clazz = clazz, cacheStore = cacheStore) }
    }
  }

  private fun getStoreInfo(id: String, clazz: Class<*>): StoreInfo {
    val info = _stores[id] ?: libError("id:${id} has not been registered when create ${clazz.name}")
    if (info.clazz != clazz) libError("id:${id} has bound to ${info.clazz.name} when bind ${clazz.name}")
    return info
  }

  private class StoreInfo(
    val clazz: Class<*>,
    val cacheStore: CacheStore? = null,
  )
}
