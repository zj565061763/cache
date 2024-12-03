package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.limitCount

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
        clazz: Class<*>,
    ): CacheStore {
        if (_isClosed) libError("Group:${group} Closed")

        _stores[id]?.also { info ->
            if (info.clazz != clazz) {
                libError("id:${id} has bound to ${info.clazz.name} when bind ${clazz.name}")
            }
            return info.cacheStore
        }

        val config = CacheConfig.get()
        return when (cacheSizePolicy) {
            is CacheSizePolicy.Unlimited -> config.newCacheStore()
            is CacheSizePolicy.LimitCount -> config.newCacheStore().limitCount(cacheSizePolicy.count)
        }.also { cacheStore ->
            _stores[id] = StoreInfo(cacheStore, clazz)
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
        val clazz: Class<*>,
    )
}

internal sealed interface CacheSizePolicy {
    /** 不限制大小 */
    data object Unlimited : CacheSizePolicy

    /** 限制个数 */
    data class LimitCount(val count: Int) : CacheSizePolicy
}