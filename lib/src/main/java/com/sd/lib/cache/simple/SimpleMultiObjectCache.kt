package com.sd.lib.cache.simple

import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.impl.ObjectHandler

internal class SimpleMultiObjectCache<T>(
    val objectClass: Class<T>,
    info: CacheInfo,
) : MultiObjectCache<T> {

    private val _objectHandler by lazy {
        object : ObjectHandler(info) {
            override val keyPrefix: String
                get() = "multi_object_"
        }
    }

    private fun transformKey(key: String): String {
        return "${objectClass.name}_$key"
    }

    override fun put(key: String, value: T?): Boolean {
        if (key.isEmpty()) return false
        return _objectHandler.putCache(transformKey(key), value)
    }

    override fun get(key: String): T? {
        if (key.isEmpty()) return null
        val cache = _objectHandler.getCache(transformKey(key), objectClass) ?: return null
        return cache as T
    }

    override fun remove(key: String): Boolean {
        if (key.isEmpty()) return false
        return _objectHandler.removeCache(transformKey(key))
    }

    override fun contains(key: String): Boolean {
        if (key.isEmpty()) return false
        return _objectHandler.containsCache(transformKey(key))
    }
}