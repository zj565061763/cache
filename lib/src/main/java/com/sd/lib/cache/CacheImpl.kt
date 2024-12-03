package com.sd.lib.cache

import com.sd.lib.cache.impl.MultiObjectCacheImpl
import com.sd.lib.cache.impl.SingleObjectCacheImpl
import com.sd.lib.cache.store.CacheStore

internal fun interface CacheStoreOwner {
    fun getCacheStore(): CacheStore
}

internal class CacheImpl(
    private val cacheStoreOwner: CacheStoreOwner,
) : Cache, CacheInfo {
    override val cacheStore: CacheStore get() = cacheStoreOwner.getCacheStore()
    override val objectConverter: Cache.ObjectConverter get() = CacheConfig.get().objectConverter
    override val exceptionHandler: Cache.ExceptionHandler get() = CacheConfig.get().exceptionHandler

    override fun <T> single(clazz: Class<T>): Cache.SingleObjectCache<T> {
        val cacheType = clazz.requireCacheType()
        return SingleObjectCacheImpl(
            cacheInfo = this@CacheImpl,
            clazz = clazz,
            id = cacheType.id,
        )
    }

    override fun <T> multi(clazz: Class<T>): Cache.MultiObjectCache<T> {
        val cacheType = clazz.requireCacheType()
        return MultiObjectCacheImpl(
            cacheInfo = this@CacheImpl,
            clazz = clazz,
            id = cacheType.id,
        )
    }
}

private fun Class<*>.requireCacheType(): CacheType {
    return requireNotNull(getAnnotation(CacheType::class.java)) {
        "Annotation ${CacheType::class.java.simpleName} was not found in $name"
    }.also {
        require(it.id.isNotEmpty()) {
            "CacheType.id is empty in $name"
        }
    }
}