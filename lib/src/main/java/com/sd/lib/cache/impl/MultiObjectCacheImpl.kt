package com.sd.lib.cache.impl

import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.CacheError
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
        if (key.isEmpty()) throw CacheError("key is empty")
        return _keyPrefix + key
    }

    private fun unpackKey(key: String): String {
        if (key.isEmpty()) throw CacheError("key is empty")
        return key.removePrefix(_keyPrefix)
    }

    override fun put(key: String, value: T?): Boolean {
        if (key.isEmpty()) return false
        if (value == null) return false
        @Suppress("NAME_SHADOWING")
        val key = packKey(key)
        return _objectHandler.putCache(key, value, objectClass)
    }

    override fun get(key: String): T? {
        if (key.isEmpty()) return null
        @Suppress("NAME_SHADOWING")
        val key = packKey(key)
        return _objectHandler.getCache(key, objectClass)
    }

    override fun remove(key: String) {
        if (key.isEmpty()) return
        @Suppress("NAME_SHADOWING")
        val key = packKey(key)
        _objectHandler.removeCache(key)
    }

    override fun contains(key: String): Boolean {
        if (key.isEmpty()) return false
        @Suppress("NAME_SHADOWING")
        val key = packKey(key)
        return _objectHandler.containsCache(key)
    }

    override fun keys(): Array<String> {
        val keys = _objectHandler.keys { unpackKey(it) }
        return keys.toTypedArray()
    }
}