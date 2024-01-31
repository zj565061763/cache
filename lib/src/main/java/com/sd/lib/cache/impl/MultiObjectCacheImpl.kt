package com.sd.lib.cache.impl

import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.handler.CacheHandler
import com.sd.lib.cache.handler.CacheInfo
import com.sd.lib.cache.handler.ObjectHandler

internal class MultiObjectCacheImpl<T>(
    cacheInfo: CacheInfo,
    val objectClass: Class<T>,
) : MultiObjectCache<T> {

    private val _keyPrefix: String = "${objectClass.name}_"
    private val _objectHandler: CacheHandler<T> = ObjectHandler(cacheInfo, "m_obj")

    private fun packKey(key: String): String {
        require(key.isNotEmpty()) { "key is empty" }
        return _keyPrefix + key
    }

    private fun unpackKey(key: String): String {
        require(key.isNotEmpty()) { "key is empty" }
        return key.removePrefix(_keyPrefix)
    }

    override fun put(key: String, value: T?): Boolean {
        if (key.isEmpty()) return false
        if (value == null) return false
        return _objectHandler.putCache(packKey(key), value, objectClass)
    }

    override fun get(key: String): T? {
        if (key.isEmpty()) return null
        return _objectHandler.getCache(packKey(key), objectClass)
    }

    override fun remove(key: String) {
        if (key.isEmpty()) return
        _objectHandler.removeCache(packKey(key))
    }

    override fun contains(key: String): Boolean {
        if (key.isEmpty()) return false
        return _objectHandler.containsCache(packKey(key))
    }

    override fun keys(): Array<String> {
        val keys = _objectHandler.keys { unpackKey(it) }
        return keys.toTypedArray()
    }
}