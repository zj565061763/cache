package com.sd.lib.cache.store.holder

import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.libError
import com.sd.lib.cache.libNotifyException
import com.sd.lib.cache.store.CacheStore

internal class GroupCacheStoreHolder {
    private val _groups: MutableMap<String, CacheStoreFactoryImpl> = hashMapOf()

    fun group(group: String): CacheStoreFactory {
        return _groups.getOrPut(group) { CacheStoreFactoryImpl(group) }
    }

    fun removeAndClose(group: String) {
        _groups.remove(group)?.close()
    }
}

internal interface CacheStoreFactory {
    fun getOrPut(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore
}

internal enum class CacheSizePolicy {
    /** 不限制 */
    Unlimited,

    /** 限制个数 */
    LimitCount
}

private class CacheStoreFactoryImpl(private val group: String) : CacheStoreFactory {
    private var _isClosed = false
    private val _stores: MutableMap<String, StoreInfo> = hashMapOf()

    override fun getOrPut(
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
        _stores.values.forEach {
            try {
                it.cacheStore.close()
            } catch (e: Throwable) {
                libNotifyException(e)
            }
        }
        _stores.clear()
    }

    private class StoreInfo(
        val cacheStore: CacheStore,
        val cacheSizePolicy: CacheSizePolicy,
    )
}