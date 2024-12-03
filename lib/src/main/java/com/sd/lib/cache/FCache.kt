package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.EmptyActiveGroupCacheStore

object FCache {
    /** DefaultGroup */
    private const val DEFAULT_GROUP = "com.sd.lib.cache.default.group"
    /** DefaultGroup的[CacheStoreFactory] */
    private val _defaultGroupCacheStoreFactory = CacheStoreFactory(DEFAULT_GROUP)

    /** ActiveGroup */
    @Volatile
    private var _activeGroup = ""
    /** ActiveGroup的[CacheStoreFactory] */
    private var _activeGroupCacheStoreFactory: CacheStoreFactory? = null

    /** 缓存所有的[Cache] */
    private val _caches = mutableMapOf<Class<*>, Cache<*>>()

    @JvmStatic
    fun <T> get(clazz: Class<T>): Cache<T> {
        return cacheLock {
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
        cacheLock { _activeGroup = group }
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

        defaultGroupCache?.also {
            return CacheImpl(
                clazz = clazz,
                cacheStoreOwner = cacheStoreOwnerForDefaultGroup(
                    id = it.id,
                    cacheSizePolicy = it.limitCount.cacheSizePolicy(),
                    clazz = clazz,
                )
            )
        }

        activeGroupCache?.also {
            return CacheImpl(
                clazz = clazz,
                cacheStoreOwner = cacheStoreOwnerForActiveGroup(
                    id = it.id,
                    cacheSizePolicy = it.limitCount.cacheSizePolicy(),
                    clazz = clazz,
                )
            )
        }

        error("This should not happen")
    }

    private fun cacheStoreOwnerForDefaultGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        clazz: Class<*>,
    ): CacheStoreOwner {
        require(id.isNotEmpty()) { "id is empty" }
        return CacheStoreOwner {
            _defaultGroupCacheStoreFactory.create(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                clazz = clazz,
            )
        }
    }

    private fun cacheStoreOwnerForActiveGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        clazz: Class<*>,
    ): CacheStoreOwner {
        require(id.isNotEmpty()) { "id is empty" }
        return CacheStoreOwner {
            getActiveGroupCacheStore(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                clazz = clazz,
            )
        }
    }

    private fun getActiveGroupCacheStore(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        clazz: Class<*>,
    ): CacheStore {
        val activeGroup = _activeGroup
        if (activeGroup.isEmpty()) {
            _activeGroupCacheStoreFactory?.close()
            _activeGroupCacheStoreFactory = null
            return EmptyActiveGroupCacheStore
        }

        val storeFactory = _activeGroupCacheStoreFactory?.takeIf { it.group == activeGroup }
            ?: CacheStoreFactory(activeGroup).also { newFactory ->
                _activeGroupCacheStoreFactory?.close()
                _activeGroupCacheStoreFactory = newFactory
            }

        return storeFactory.create(
            id = id,
            cacheSizePolicy = cacheSizePolicy,
            clazz = clazz,
        )
    }
}

internal fun <R> cacheLock(block: () -> R): R {
    return synchronized(
        lock = FCache,
        block = block,
    )
}