package com.sd.lib.cache

import com.sd.lib.cache.handler.CacheInfo
import com.sd.lib.cache.impl.MultiObjectCacheImpl
import com.sd.lib.cache.impl.SingleObjectCacheImpl
import com.sd.lib.cache.store.CacheStore

internal fun interface CacheStoreOwner {
    fun getCacheStore(): CacheStore
}

internal class CacheImpl(
    private val cacheStoreOwner: CacheStoreOwner
) : Cache {

    private var _cObject: SingleObjectCacheImpl<*>? = null
    private var _cObjects: MultiObjectCacheImpl<*>? = null

    private val _cacheInfo = object : CacheInfo {
        override val cacheStore: CacheStore get() = cacheStoreOwner.getCacheStore()
        override val objectConverter: Cache.ObjectConverter get() = CacheConfig.get().objectConverter
        override val exceptionHandler: Cache.ExceptionHandler get() = CacheConfig.get().exceptionHandler
    }

    override fun <T> o(clazz: Class<T>): Cache.SingleObjectCache<T> {
        _cObject?.let { cache ->
            if (cache.objectClass == clazz) {
                @Suppress("UNCHECKED_CAST")
                return (cache as Cache.SingleObjectCache<T>)
            }
        }
        return SingleObjectCacheImpl(_cacheInfo, clazz).also {
            _cObject = it
        }
    }

    override fun <T> oo(clazz: Class<T>): Cache.MultiObjectCache<T> {
        _cObjects?.let { cache ->
            if (cache.objectClass == clazz) {
                @Suppress("UNCHECKED_CAST")
                return (cache as Cache.MultiObjectCache<T>)
            }
        }
        return MultiObjectCacheImpl(_cacheInfo, clazz).also {
            _cObjects = it
        }
    }
}