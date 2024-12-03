package com.sd.lib.cache.impl

import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheHandler
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.newCacheHandler

internal class SingleObjectCacheImpl<T>(
    cacheInfo: CacheInfo,
    private val clazz: Class<T>,
    id: String,
) : Cache.SingleObjectCache<T> {

    private val _key = "o_${id}"
    private val _handler: CacheHandler<T> = newCacheHandler(cacheInfo)

    override fun put(value: T?): Boolean {
        if (value == null) return false
        return _handler.putCache(_key, value, clazz)
    }

    override fun get(): T? {
        return _handler.getCache(_key, clazz)
    }

    override fun remove() {
        _handler.removeCache(_key)
    }

    override fun contains(): Boolean {
        return _handler.containsCache(_key)
    }
}