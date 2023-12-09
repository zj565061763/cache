package com.sd.lib.cache.handler

/**
 * 缓存处理接口
 */
internal interface CacheHandler<T> {
    fun putCache(key: String, value: T, clazz: Class<*>?): Boolean

    fun getCache(key: String, clazz: Class<*>?): T?

    fun removeCache(key: String)

    fun containsCache(key: String): Boolean
}