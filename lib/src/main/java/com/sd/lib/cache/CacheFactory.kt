package com.sd.lib.cache

import com.sd.lib.cache.store.holder.CacheSizePolicy
import com.sd.lib.cache.store.holder.CacheStoreManager
import com.sd.lib.cache.store.limitCount

interface CacheFactory {
    /**
     * 无限制缓存
     */
    fun unlimited(id: String): Cache

    /**
     * 限制[limit]个数的缓存
     */
    fun limitCount(id: String, limit: Int): Cache
}

internal class DefaultGroupCacheFactory : CacheFactory {
    override fun unlimited(id: String): Cache {
        val cacheStoreOwner = CacheStoreManager.cacheStoreOwnerForDefaultGroup(
            id = id,
            cacheSizePolicy = CacheSizePolicy.Unlimited,
            factory = { it.newCacheStore() },
        )
        return CacheImpl(cacheStoreOwner)
    }

    override fun limitCount(id: String, limit: Int): Cache {
        val cacheStoreOwner = CacheStoreManager.cacheStoreOwnerForDefaultGroup(
            id = id,
            cacheSizePolicy = CacheSizePolicy.LimitCount,
            factory = { it.newCacheStore().limitCount(limit) },
        )
        return CacheImpl(cacheStoreOwner)
    }
}

internal class CurrentGroupCacheFactory : CacheFactory {
    override fun unlimited(id: String): Cache {
        val cacheStoreOwner = CacheStoreManager.cacheStoreOwnerForCurrentGroup(
            id = id,
            cacheSizePolicy = CacheSizePolicy.Unlimited,
            factory = { it.newCacheStore() },
        )
        return CacheImpl(cacheStoreOwner)
    }

    override fun limitCount(id: String, limit: Int): Cache {
        val cacheStoreOwner = CacheStoreManager.cacheStoreOwnerForCurrentGroup(
            id = id,
            cacheSizePolicy = CacheSizePolicy.LimitCount,
            factory = { it.newCacheStore().limitCount(limit) },
        )
        return CacheImpl(cacheStoreOwner)
    }
}