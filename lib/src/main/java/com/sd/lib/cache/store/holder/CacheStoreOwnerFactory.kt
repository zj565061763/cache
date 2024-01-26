package com.sd.lib.cache.store.holder

import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.CacheLock
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.EmptyCacheStore

internal object CacheStoreOwnerFactory {
    private const val DefaultGroup = "com.sd.lib.cache.default.group"

    private val _holder = GroupCacheStoreHolder()

    /** 当前Group */
    private var _currentGroup = ""

    /**
     * 设置当前Group
     */
    fun setCurrentGroup(group: String) {
        synchronized(CacheLock) {
            val oldGroup = _currentGroup
            if (oldGroup == group) return

            require(group != DefaultGroup)
            _currentGroup = group

            _holder.remove(oldGroup)
        }
    }

    fun createDefaultGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStoreOwner {
        val cacheStore = getOrPut(
            group = DefaultGroup,
            id = id,
            cacheSizePolicy = cacheSizePolicy,
            factory = factory,
        )
        return CacheStoreOwner { cacheStore }
    }

    fun createCurrentGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStoreOwner {
        return CacheStoreOwner {
            synchronized(CacheLock) {
                val currentGroup = _currentGroup
                if (currentGroup.isEmpty()) {
                    EmptyCacheStore
                } else {
                    getOrPut(
                        group = currentGroup,
                        id = id,
                        cacheSizePolicy = cacheSizePolicy,
                        factory = factory,
                    )
                }
            }
        }
    }

    private fun getOrPut(
        group: String,
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore {
        synchronized(CacheLock) {
            return _holder.group(group).getOrPut(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                factory = factory,
            )
        }
    }
}