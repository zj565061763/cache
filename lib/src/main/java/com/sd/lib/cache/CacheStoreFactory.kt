package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

internal class CacheStoreFactory(
    val group: String,
) {
    private var _isClosed = false
    private val _stores = mutableMapOf<String, StoreInfo>()

    init {
        require(group.isNotEmpty()) { "group is empty" }
    }

    fun create(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore {
        if (_isClosed) libError("Group:${group} Closed")

        _stores[id]?.also { info ->
            if (info.cacheSizePolicy != cacheSizePolicy) libError("ID (${id}) exist with ${cacheSizePolicy.name}")
            return info.cacheStore
        }

        val config = CacheConfig.get()
        return factory(config).also { cacheStore ->
            _stores[id] = StoreInfo(cacheStore, cacheSizePolicy)
            config.initCacheStore(cacheStore, group = group, id = id)
        }
    }

    fun close() {
        _isClosed = true
        while (_stores.isNotEmpty()) {
            _stores.keys.toTypedArray().forEach { key ->
                _stores.remove(key)?.cacheStore?.also {
                    try {
                        it.close()
                    } catch (e: Throwable) {
                        libNotifyException(e)
                    }
                }
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