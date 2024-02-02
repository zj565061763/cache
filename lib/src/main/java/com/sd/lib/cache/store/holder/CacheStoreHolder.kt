package com.sd.lib.cache.store.holder

import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.CacheError
import com.sd.lib.cache.libNotifyException
import com.sd.lib.cache.store.CacheStore

internal class GroupCacheStoreHolder {
    private val _groups: MutableMap<String, CacheStoreHolderImpl> = hashMapOf()

    fun group(group: String): CacheStoreHolder {
        if (group.isEmpty()) throw CacheError("group is empty.")
        return _groups.getOrPut(group) { CacheStoreHolderImpl(group) }
    }

    fun removeAndClose(group: String) {
        _groups.remove(group)?.close()
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
    private var _isClosed = false
    private val _stores: MutableMap<String, StoreInfo> = hashMapOf()

    override fun getOrPut(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore {
        if (_isClosed) throw CacheError("Closed.")
        require(id.isNotEmpty())
        val info = _stores[id]
        return if (info != null) {
            if (info.cacheSizePolicy != cacheSizePolicy) {
                throw CacheError("ID (${id}) exist with ${cacheSizePolicy.name}.")
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

internal enum class CacheSizePolicy {
    /** 不限制 */
    Unlimited,

    /** 限制个数 */
    LimitCount
}