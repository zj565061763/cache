package com.sd.lib.cache

import com.sd.lib.cache.handler.BooleanHandler
import com.sd.lib.cache.handler.BytesHandler
import com.sd.lib.cache.handler.CacheInfo
import com.sd.lib.cache.handler.DoubleHandler
import com.sd.lib.cache.handler.FloatHandler
import com.sd.lib.cache.handler.IntHandler
import com.sd.lib.cache.handler.LongHandler
import com.sd.lib.cache.handler.StringHandler
import com.sd.lib.cache.impl.MultiObjectCacheImpl
import com.sd.lib.cache.impl.SingleObjectCacheImpl
import com.sd.lib.cache.store.CacheStore

internal class CacheImpl(store: CacheStore) : Cache {

    private var _cInt: Cache.CommonCache<Int>? = null
    private var _cLong: Cache.CommonCache<Long>? = null
    private var _cFloat: Cache.CommonCache<Float>? = null
    private var _cDouble: Cache.CommonCache<Double>? = null
    private var _cBoolean: Cache.CommonCache<Boolean>? = null
    private var _cString: Cache.CommonCache<String>? = null
    private var _cBytes: Cache.CommonCache<ByteArray>? = null

    private var _cObject: SingleObjectCacheImpl<*>? = null
    private var _cObjects: MultiObjectCacheImpl<*>? = null

    private val _cacheInfo = object : CacheInfo {
        override val cacheStore: CacheStore get() = store
        override val objectConverter: Cache.ObjectConverter get() = CacheConfig.get().objectConverter
        override val exceptionHandler: Cache.ExceptionHandler get() = CacheConfig.get().exceptionHandler
    }

    //---------- Cache start ----------

    override fun cInt(): Cache.CommonCache<Int> {
        return _cInt ?: IntHandler(_cacheInfo).also { _cInt = it }
    }

    override fun cLong(): Cache.CommonCache<Long> {
        return _cLong ?: LongHandler(_cacheInfo).also { _cLong = it }
    }

    override fun cFloat(): Cache.CommonCache<Float> {
        return _cFloat ?: FloatHandler(_cacheInfo).also { _cFloat = it }
    }

    override fun cDouble(): Cache.CommonCache<Double> {
        return _cDouble ?: DoubleHandler(_cacheInfo).also { _cDouble = it }
    }

    override fun cBoolean(): Cache.CommonCache<Boolean> {
        return _cBoolean ?: BooleanHandler(_cacheInfo).also { _cBoolean = it }
    }

    override fun cString(): Cache.CommonCache<String> {
        return _cString ?: StringHandler(_cacheInfo).also { _cString = it }
    }

    override fun cBytes(): Cache.CommonCache<ByteArray> {
        return _cBytes ?: BytesHandler(_cacheInfo).also { _cBytes = it }
    }

    override fun <T> cObject(clazz: Class<T>): Cache.SingleObjectCache<T> {
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

    override fun <T> cObjects(clazz: Class<T>): Cache.MultiObjectCache<T> {
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

    //---------- Cache end ----------
}