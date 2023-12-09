package com.sd.lib.cache.simple

import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.impl.ObjectHandler

internal class SingleObjectCacheImpl<T>(
    cacheInfo: CacheInfo,
    val objectClass: Class<T>,
) : Cache.SingleObjectCache<T> {

    private val _key = objectClass.name
    private val _handler = ObjectHandler(cacheInfo, "single_object")

    override fun put(value: T?): Boolean {
        if (value == null) return false
        return _handler.putCache(_key, value, objectClass)
    }

    override fun get(): T? {
        val cache = _handler.getCache(_key, objectClass) ?: return null
        @Suppress("UNCHECKED_CAST")
        return cache as T
    }

    override fun remove(): Boolean {
        return _handler.remove(_key)
    }

    override fun contains(): Boolean {
        return _handler.contains(_key)
    }
}