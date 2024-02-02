package com.sd.lib.cache

import com.sd.lib.cache.store.holder.CacheStoreOwnerFactory

object FCache {
    private const val DefaultID = "com.sd.lib.cache.default.id"

    /** 默认无限制缓存 */
    private val _defaultCache: Cache = defaultGroup().unlimited(DefaultID)

    /**
     * 默认Group无限制缓存
     */
    @JvmStatic
    fun get(): Cache = _defaultCache

    /**
     * 设置当前Group
     */
    @JvmStatic
    fun setCurrentGroup(group: String) {
        CacheStoreOwnerFactory.setCurrentGroup(group)
    }

    /**
     * 默认Group
     */
    @JvmStatic
    fun defaultGroup(): CacheFactory = DefaultGroupCacheFactory()

    /**
     * 当前Group
     */
    @JvmStatic
    fun currentGroup(): CacheFactory = CurrentGroupCacheFactory()
}

internal val CacheLock: Any = FCache