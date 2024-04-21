package com.sd.lib.cache

object FCache {
    private const val DEFAULT_ID = "com.sd.lib.cache.default.id"

    /** DefaultGroup无限制缓存 */
    private val _defaultCache: Cache = defaultGroupFactory().unlimited(DEFAULT_ID)

    /** ActiveGroup无限制缓存 */
    private val _activeCache: Cache = activeGroupFactory().unlimited(DEFAULT_ID)

    /**
     * DefaultGroup无限制缓存
     */
    @JvmStatic
    fun getDefault(): Cache = _defaultCache

    /**
     * ActiveGroup无限制缓存
     */
    fun getActive(): Cache = _activeCache

    /**
     * ActiveGroup
     */
    @JvmStatic
    fun getActiveGroup(): String = CacheManager.getActiveGroup()

    /**
     * 设置ActiveGroup
     */
    @JvmStatic
    fun setActiveGroup(group: String) = CacheManager.setActiveGroup(group)

    /**
     * DefaultGroup工厂
     */
    @JvmStatic
    fun defaultGroupFactory(): CacheFactory = DefaultGroupCacheFactory()

    /**
     * ActiveGroup工厂
     */
    @JvmStatic
    fun activeGroupFactory(): CacheFactory = ActiveGroupCacheFactory()
}