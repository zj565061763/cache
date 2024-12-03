package com.sd.lib.cache.impl

import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.CacheHandler
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.libError
import com.sd.lib.cache.newCacheHandler

internal class MultiObjectCacheImpl<T>(
    cacheInfo: CacheInfo,
    private val clazz: Class<T>,
    id: String,
) : MultiObjectCache<T> {
    private val _keyPrefix = "oo_${id}_"
    private val _objectHandler: CacheHandler<T> = newCacheHandler(cacheInfo)

    private fun packKey(key: String): String {
        if (key.isEmpty()) libError("key is empty")
        return _keyPrefix + key
    }

    private fun unpackKey(key: String): String {
        if (key.isEmpty()) libError("key is empty")
        return key.removePrefix(_keyPrefix)
    }

    override fun put(key: String, value: T?): Boolean {
        if (value == null) return false
        return _objectHandler.putCache(packKey(key), value, clazz)
    }

    override fun get(key: String): T? {
        return _objectHandler.getCache(packKey(key), clazz)
    }

    override fun remove(key: String) {
        _objectHandler.removeCache(packKey(key))
    }

    override fun contains(key: String): Boolean {
        return _objectHandler.containsCache(packKey(key))
    }

    override fun keys(): List<String> {
        return _objectHandler.keys { keys ->
            keys.asSequence()
                .filter { it.startsWith(_keyPrefix) }
                .map { unpackKey(it) }
                .toList()
        }
    }
}