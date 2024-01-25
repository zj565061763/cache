package com.sd.lib.cache.impl

import com.sd.lib.cache.Cache
import com.sd.lib.cache.handler.CacheInfo
import com.sd.lib.cache.handler.ObjectHandler

internal class SingleObjectCacheImpl<T>(
    cacheInfo: CacheInfo,
    val objectClass: Class<T>,
) : Cache.SingleObjectCache<T> {

    private val _key = objectClass.name
    private val _handler = ObjectHandler<T>(cacheInfo, "single_object")

    override fun put(value: T?): Boolean {
        if (value == null) return false
        return _handler.putCache(_key, value, objectClass)
    }

    override fun get(): T? {
        return _handler.getCache(_key, objectClass)
    }

    override fun remove() {
        _handler.remove(_key)
    }

    override fun contains(): Boolean {
        return _handler.contains(_key)
    }
}