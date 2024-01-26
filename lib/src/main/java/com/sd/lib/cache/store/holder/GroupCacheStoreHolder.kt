package com.sd.lib.cache.store.holder

import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.store.CacheStore

internal class GroupsCacheStoreHolder {
    private val _groups: MutableMap<String, GroupCacheStoreHolder> = hashMapOf()

    fun group(group: String): GroupCacheStoreHolder {
        return _groups.getOrPut(group) { GroupCacheStoreHolderImpl(group) }
    }
}

internal interface GroupCacheStoreHolder {
    fun getOrPut(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore
}

private class GroupCacheStoreHolderImpl(
    private val group: String
) : GroupCacheStoreHolder {
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
            factory(config).also { store ->
                _stores[id] = StoreInfo(store, cacheSizePolicy)
                config.initCacheStore(store, group = group, id = id)
            }
        }
    }

    private data class StoreInfo(
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