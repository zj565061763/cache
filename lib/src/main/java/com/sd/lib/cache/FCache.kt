package com.sd.lib.cache

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