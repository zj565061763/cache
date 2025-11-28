package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.EmptyCacheStore

object FCache {
  /** DefaultGroup */
  private const val DEFAULT_GROUP = "com.sd.lib.cache.group.default"
  /** DefaultGroup的[CacheStoreFactory] */
  private val _defaultGroupCacheStoreFactory = CacheStoreFactory(DEFAULT_GROUP)

  /** ActiveGroup */
  @Volatile
  private var _activeGroup = ""
  /** ActiveGroup的[CacheStoreFactory] */
  private var _activeGroupCacheStoreFactory: CacheStoreFactory? = null

  /** 缓存所有的[Cache] */
  private val _caches = mutableMapOf<Class<*>, Cache<*>>()

  /** 多进程锁 */
  internal val multiProcessLock by lazy {
    val lockFile = CacheConfig.get().directory.resolve("cache.lock")
    MultiProcessLock(lockFile = lockFile, currentProcessLock = FCache)
  }

  /** 获取[clazz]对应的[Cache] */
  @JvmStatic
  fun <T> get(clazz: Class<T>): Cache<T> {
    return synchronized(FCache) {
      val cache = _caches.getOrPut(clazz) { newCache(clazz) }
      @Suppress("UNCHECKED_CAST")
      cache as Cache<T>
    }
  }

  @JvmStatic
  fun getActiveGroup(): String {
    return _activeGroup
  }

  @JvmStatic
  fun setActiveGroup(group: String) {
    require(group != DEFAULT_GROUP) { "Require not default group" }
    synchronized(FCache) { _activeGroup = group }
  }

  private fun <T> newCache(clazz: Class<T>): Cache<T> {
    val defaultGroupCache = clazz.getAnnotation(DefaultGroupCache::class.java)
    val activeGroupCache = clazz.getAnnotation(ActiveGroupCache::class.java)

    when {
      defaultGroupCache == null && activeGroupCache == null -> {
        throw IllegalArgumentException("Annotation ${DefaultGroupCache::class.java.simpleName} or ${ActiveGroupCache::class.java.simpleName} was not found in $clazz")
      }
      defaultGroupCache != null && activeGroupCache != null -> {
        throw IllegalArgumentException("Can not use both ${DefaultGroupCache::class.java.simpleName} and ${ActiveGroupCache::class.java.simpleName} in $clazz")
      }
    }

    defaultGroupCache?.also { cache ->
      val id = cache.id.ifBlank { throw IllegalArgumentException("${DefaultGroupCache::class.java.simpleName}.id is blank") }
      return CacheImpl(clazz) {
        _defaultGroupCacheStoreFactory.create(id = id, clazz = clazz)
      }
    }

    activeGroupCache?.also { cache ->
      val id = cache.id.ifBlank { throw IllegalArgumentException("${ActiveGroupCache::class.java.simpleName}.id is blank") }
      return CacheImpl(clazz) {
        getActiveGroupCacheStore(id = id, clazz = clazz)
      }
    }

    error("This should not happen")
  }

  private fun getActiveGroupCacheStore(id: String, clazz: Class<*>): CacheStore {
    val activeGroup = _activeGroup
    if (activeGroup.isBlank()) {
      _activeGroupCacheStoreFactory?.close()
      _activeGroupCacheStoreFactory = null
      return EmptyCacheStore
    }

    val storeFactory = _activeGroupCacheStoreFactory?.takeIf { it.group == activeGroup }
      ?: CacheStoreFactory(activeGroup).also { newFactory ->
        _activeGroupCacheStoreFactory?.close()
        _activeGroupCacheStoreFactory = newFactory
      }

    return storeFactory.create(id = id, clazz = clazz)
  }
}

/** 多进程锁 */
internal fun <T> multiProcessLock(block: () -> T): T {
  return FCache.multiProcessLock.lock(block)
}