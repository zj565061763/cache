package com.sd.lib.cache.impl

import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.ObjectHandler

internal class MultiObjectCacheImpl<T>(
    cacheInfo: CacheInfo,
    val objectClass: Class<T>,
) : MultiObjectCache<T> {
    private val _objectHandler = ObjectHandler(cacheInfo, "multi_object")

    private fun transformKey(key: String): String {
        require(key.isNotEmpty()) { "key is empty" }
        return "${objectClass.name}_$key"
    }

    override fun put(key: String, value: T?): Boolean {
        if (key.isEmpty()) return false
        if (value == null) return false
        return _objectHandler.putCache(transformKey(key), value, objectClass)
    }

    override fun get(key: String): T? {
        if (key.isEmpty()) return null
        val cache = _objectHandler.getCache(transformKey(key), objectClass) ?: return null
        @Suppress("UNCHECKED_CAST")
        return cache as T
    }

    override fun remove(key: String) {
        if (key.isEmpty()) return
        _objectHandler.removeCache(transformKey(key))
    }

    override fun contains(key: String): Boolean {
        if (key.isEmpty()) return false
        return _objectHandler.containsCache(transformKey(key))
    }
}