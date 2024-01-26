package com.sd.lib.cache.store.holder

import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.store.CacheStore

internal class GroupCacheStoreHolder {
    private val _groups: MutableMap<String, CacheStoreHolder> = hashMapOf()

    fun group(group: String): CacheStoreHolder {
        return _groups.getOrPut(group) { CacheStoreHolderImpl(group) }
    }
}

internal interface CacheStoreHolder {
    fun getOrPut(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore
}

private class CacheStoreHolderImpl(
    private val group: String
) : CacheStoreHolder {
    private val _stores: MutableMap<String, StoreInfo> = hashMapOf()

    init {
        require(group.isNotEmpty())
    }

    override fun getOrPut(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore {
        require(id.isNotEmpty())
        val info = _stores[id]
        return if (info != null) {
            check(info.cacheSizePolicy == cacheSizePolicy) {
                "ID (${id}) exist with ${cacheSizePolicy.name}."
            }
            info.cacheStore
        } else {
            val config = CacheConfig.get()
            factory(config).also { cacheStore ->
                _stores[id] = StoreInfo(cacheStore, cacheSizePolicy)
                config.initCacheStore(cacheStore, group = group, id = id)
            }
        }
    }

    private class StoreInfo(
        val cacheStore: CacheStore,
        val cacheSizePolicy: CacheSizePolicy,
    )
}

internal enum class CacheSizePolicy {
    /** 不限制 */
    Unlimited,

    /** 限制个数 */
    LimitCount
}