package com.sd.lib.cache

/**
 * DefaultGroup缓存
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DefaultGroupCache(
  /** 缓存id，在该组中不能重复 */
  val id: String,
  /** 是否支持多进程 */
  val multiProcess: Boolean = false,
)

object FCache {
  /** DefaultGroup */
  private const val DEFAULT_GROUP = "com.sd.lib.cache.group.default"
  /** DefaultGroup的[GroupCacheStoreFactory] */
  private val _defaultGroupCacheStoreFactory = GroupCacheStoreFactory(DEFAULT_GROUP)

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
    val annotation = clazz.getAnnotation(DefaultGroupCache::class.java)
    require(annotation != null) { "Annotation ${DefaultGroupCache::class.java.simpleName} was not found in $clazz" }

    val id = annotation.id
    require(id.isNotBlank()) { "${DefaultGroupCache::class.java.simpleName}.id is blank in $clazz" }

    return CacheImpl(clazz, annotation.multiProcess) { _defaultGroupCacheStoreFactory.create(id = id, clazz = clazz) }
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