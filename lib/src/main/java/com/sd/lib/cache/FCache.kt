package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.EmptyCacheStore

object FCache {
    private const val DefaultGroup = "com.sd.lib.cache.default.group"
    private const val DefaultID = "com.sd.lib.cache.default.id"

    /** 当前组 */
    private var _currentGroup = ""

    /** 保存所有仓库 */
    private val _stores: MutableMap<String, StoreInfo> = hashMapOf()

    /** 默认无限制缓存 */
    private val _defaultCache: Cache = CacheFactory.groupDefault().unlimited(DefaultID)

    /**
     * 默认无限制缓存
     */
    @JvmStatic
    fun get(): Cache = _defaultCache

    internal fun getCacheStore(
        group: String?,
        id: String,
    ): CacheStore {
        return when (group) {
            // 默认Group
            null -> {
                val fullID = fullID(group = DefaultGroup, id = id)
                checkNotNull(_stores[fullID]).cacheStore
            }

            // 当前Group
            "" -> {
                val currentGroup = _currentGroup
                if (currentGroup.isEmpty()) {
                    EmptyCacheStore
                } else {
                    val fullID = fullID(group = currentGroup, id = id)
                    checkNotNull(_stores[fullID]).cacheStore
                }
            }

            // 自定义Group
            else -> {
                val fullID = fullID(group = group, id = id)
                checkNotNull(_stores[fullID]).cacheStore
            }
        }
    }

    internal fun newCache(
        group: String?,
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): Cache {
        initCacheStore(
            group = group,
            id = id,
            cacheSizePolicy = cacheSizePolicy,
            factory = factory,
        )
        return CacheImpl(group = group, id = id)
    }

    private fun initCacheStore(
        group: String?,
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ) {
        val fullID = fullID(group = group ?: DefaultGroup, id = id)
        val config = CacheConfig.get()
        synchronized(Cache::class.java) {
            _stores[fullID]?.let { info ->
                check(info.cacheSizePolicy == cacheSizePolicy) {
                    "ID $id exist with ${cacheSizePolicy.name}."
                }
            }
            factory(config).also { store ->
                _stores[fullID] = StoreInfo(store, cacheSizePolicy)
                config.initStore(store, fullID)
            }
        }
    }
}

private data class StoreInfo(
    val cacheStore: CacheStore,
    val cacheSizePolicy: CacheSizePolicy,
)

internal enum class CacheSizePolicy {
    Unlimited,
    LimitCount
}

private fun fullID(group: String, id: String): String {
    require(group.isNotEmpty())
    require(id.isNotEmpty())
    return "${group}:${id}"
}