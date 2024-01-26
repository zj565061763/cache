package com.sd.lib.cache

import com.sd.lib.cache.store.limitCount

interface CacheFactory {
    /**
     * 无限制缓存
     */
    fun unlimited(id: String): Cache

    /**
     * 限制个数的缓存
     * @param id 必须保证唯一性
     */
    fun limitCount(id: String, limit: Int): Cache

    companion object {
        /**
         * 默认Group
         */
        fun groupDefault(): CacheFactory = GroupCacheFactory(null)

        /**
         * 当前Group
         */
        fun groupCurrent(): CacheFactory = GroupCacheFactory("")
    }
}

internal class GroupCacheFactory(
    private val group: String?
) : CacheFactory {
    override fun unlimited(id: String): Cache {
        return FCache.newCache(
            group = group,
            id = id,
            cacheSizePolicy = CacheSizePolicy.Unlimited,
        ) {
            it.newStore()
        }
    }

    override fun limitCount(id: String, limit: Int): Cache {
        return FCache.newCache(
            group = group,
            id = id,
            cacheSizePolicy = CacheSizePolicy.LimitCount,
        ) {
            it.newStore().limitCount(limit)
        }
    }
}