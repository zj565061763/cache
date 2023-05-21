package com.sd.lib.cache.simple

import com.sd.lib.cache.Cache.ObjectCache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.impl.ObjectHandler

internal class SimpleObjectCache(info: CacheInfo) : ObjectCache {
    private val _handler = ObjectHandler(info, "object")

    override fun put(value: Any?): Boolean {
        if (value == null) return false
        val key = value.javaClass.name
        return _handler.putCache(key, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(clazz: Class<T>): T? {
        val key = clazz.name
        val cache = _handler.getCache(key, clazz) ?: return null
        return cache as T
    }

    override fun remove(clazz: Class<*>): Boolean {
        val key = clazz.name
        return _handler.removeCache(key)
    }

    override fun contains(clazz: Class<*>): Boolean {
        val key = clazz.name
        return _handler.containsCache(key)
    }
}