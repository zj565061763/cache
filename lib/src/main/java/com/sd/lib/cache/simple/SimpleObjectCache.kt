package com.sd.lib.cache.simple

import com.sd.lib.cache.Cache.ObjectCache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.handler.ObjectHandler

class SimpleObjectCache(info: CacheInfo) : ObjectCache {

    private val _handler: ObjectHandler by lazy {
        object : ObjectHandler(info) {
            override fun getKeyPrefix(): String {
                return "object_"
            }
        }
    }

    override fun put(value: Any): Boolean {
        val key = value.javaClass.name
        return _handler.putCache(key, value)
    }

    override fun <T> get(clazz: Class<T>): T? {
        val key = clazz.name
        return _handler.getCache(key, clazz) as T
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