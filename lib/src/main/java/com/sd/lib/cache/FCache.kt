package com.sd.lib.cache

object FCache {
  private val _mapGroupCacheStoreFactory = mutableMapOf<String, GroupCacheStoreFactory>()

  /** 缓存所有的[Cache] */
  private val _caches = mutableMapOf<Class<*>, Cache<*>>()

  /** 默认的单缓存key */
  internal const val DEFAULT_SINGLE_CACHE_KEY = "com.sd.lib.cache.key.singlecache"

  /** 获取[clazz]对应的[Cache] */
  @JvmStatic
  fun <T> get(clazz: Class<T>): Cache<T> {
    return synchronized(FCache) {
      val cache = _caches.getOrPut(clazz) { newCache(clazz) }
      @Suppress("UNCHECKED_CAST")
      cache as Cache<T>
    }
  }

  private fun <T> newCache(clazz: Class<T>): Cache<T> {
    val annotation = clazz.getAnnotation(GroupCache::class.java)
    require(annotation != null) { "Annotation ${GroupCache::class.java.simpleName} was not found in $clazz" }

    val id = annotation.id
    require(id.isNotBlank()) { "${GroupCache::class.java.simpleName}.id is blank in $clazz" }

    val group = annotation.group
    require(group.isNotBlank()) { "${GroupCache::class.java.simpleName}.group is blank in $clazz" }

    val groupCacheStoreFactory = _mapGroupCacheStoreFactory.getOrPut(group) { GroupCacheStoreFactory(group) }
    return CacheImpl(
      clazz = clazz,
      lockLevel = annotation.lockLevel,
      groupLock = groupCacheStoreFactory,
      cacheStoreProvider = { groupCacheStoreFactory.create(id = id, clazz = clazz) },
    )
  }
}

object FCacheKtx {
  private val _caches = mutableMapOf<Class<*>, CacheKtx<*>>()

  fun <T> get(clazz: Class<T>): CacheKtx<T> {
    return synchronized(FCache) {
      val cache = _caches.getOrPut(clazz) { CacheKtxImpl(FCache.get(clazz) as CacheImpl<T>) }
      @Suppress("UNCHECKED_CAST")
      cache as CacheKtx<T>
    }
  }
}