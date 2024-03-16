package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore

internal class CacheStoreFactory(
    private val group: String
) {
    private var _isClosed = false
    private val _stores: MutableMap<String, StoreInfo> = hashMapOf()

    fun getOrPut(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore {
        if (_isClosed) libError("Closed.")
        val info = _stores[id]
        return if (info != null) {
            if (info.cacheSizePolicy != cacheSizePolicy) {
                libError("ID (${id}) exist with ${cacheSizePolicy.name}.")
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

    fun close() {
        _isClosed = true
        while (_stores.isNotEmpty()) {
            _stores.keys.toTypedArray().forEach { key ->
                _stores.remove(key)?.cacheStore?.let {
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