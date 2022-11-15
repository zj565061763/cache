package com.sd.lib.cache.simple

import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.impl.ObjectHandler

internal class SimpleMultiObjectCache<T>(
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
        val key = transformKey(key)
        return _objectHandler.putCache(key, value)
    }

    override fun get(key: String): T? {
        if (key.isEmpty()) return null
        val key = transformKey(key)
        val cache = _objectHandler.getCache(key, objectClass) ?: return null
        return cache as T
    }

    override fun remove(key: String): Boolean {
        if (key.isEmpty()) return false
        val key = transformKey(key)
        return _objectHandler.removeCache(key)
    }

    override fun contains(key: String): Boolean {
        if (key.isEmpty()) return false
        val key = transformKey(key)
        return _objectHandler.containsCache(key)
    }
}