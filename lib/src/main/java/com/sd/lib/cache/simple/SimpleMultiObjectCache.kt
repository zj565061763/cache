package com.sd.lib.cache.simple

import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.ObjectHandler

internal class SimpleMultiObjectCache<T>(
    info: CacheInfo,
    objectClass: Class<T>,
) : MultiObjectCache<T> {

    @JvmField
    val objectClass: Class<T> = objectClass

    private val _objectHandler: ObjectHandler by lazy {
        object : ObjectHandler(info) {
            override fun getKeyPrefix(): String {
                return "multi_object_"
            }
        }
    }

    override fun put(key: String, value: T?): Boolean {
        if (key.isEmpty()) return false
        if (value == null) return false
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