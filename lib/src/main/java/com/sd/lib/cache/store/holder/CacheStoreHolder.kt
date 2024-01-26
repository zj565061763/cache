package com.sd.lib.cache.store.holder

import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.notifyException
import com.sd.lib.cache.store.CacheStore

internal class GroupCacheStoreHolder {
    private val _groups: MutableMap<String, CacheStoreHolder> = hashMapOf()

    fun group(group: String): CacheStoreHolder {
        require(group.isNotEmpty())
        return _groups.getOrPut(group) { CacheStoreHolderImpl(group) }
    }

    fun remove(group: String) {
        _groups.remove(group)?.destroy()
    }
}

internal interface CacheStoreHolder {
    fun getOrPut(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore

    fun destroy()
}

private class CacheStoreHolderImpl(
    private val group: String
) : CacheStoreHolder {
    private var _isDestroyed = false
    private val _stores: MutableMap<String, StoreInfo> = hashMapOf()

    override fun getOrPut(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore {
        check(!_isDestroyed)
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

    override fun destroy() {
        _isDestroyed = true
        _stores.values.forEach {
            try {
                it.cacheStore.close()
            } catch (e: Throwable) {
                CacheConfig.get().exceptionHandler.notifyException(e)
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