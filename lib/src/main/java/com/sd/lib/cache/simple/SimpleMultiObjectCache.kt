package com.sd.lib.cache.simple

import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.ObjectHandler

internal class SimpleMultiObjectCache<T>(
    @JvmField
    val objectClass: Class<T>,
    info: CacheInfo,
) : MultiObjectCache<T> {

    private val _objectHandler: ObjectHandler by lazy {
        object : ObjectHandler(info) {
            override val keyPrefix: String
                get() = "multi_object_"
        }
    }

    override fun put(key: String, value: T?): Boolean {
        if (key.isEmpty()) return false
        val finalKey = "${objectClass.name}_$key"
        return _objectHandler.putCache(finalKey, value)
    }

    override fun get(key: String): T? {
        if (key.isEmpty()) return null
        val finalKey = "${objectClass.name}_$key"
        return _objectHandler.getCache(finalKey, objectClass) as T
    }

    override fun remove(key: String): Boolean {
        if (key.isEmpty()) return false
        val finalKey = "${objectClass.name}_$key"
        return _objectHandler.removeCache(finalKey)
    }

    override fun contains(key: String): Boolean {
        if (key.isEmpty()) return false
        val finalKey = "${objectClass.name}_$key"
        return _objectHandler.containsCache(finalKey)
    }
}