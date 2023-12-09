package com.sd.lib.cache

import com.sd.lib.cache.Cache.CacheStore
import com.sd.lib.cache.Cache.CommonCache
import com.sd.lib.cache.Cache.ExceptionHandler
import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.Cache.ObjectConverter
import com.sd.lib.cache.Cache.SingleObjectCache
import com.sd.lib.cache.handler.BooleanHandler
import com.sd.lib.cache.handler.BytesHandler
import com.sd.lib.cache.handler.DoubleHandler
import com.sd.lib.cache.handler.FloatHandler
import com.sd.lib.cache.handler.IntHandler
import com.sd.lib.cache.handler.LongHandler
import com.sd.lib.cache.handler.StringHandler
import com.sd.lib.cache.impl.MultiObjectCacheImpl
import com.sd.lib.cache.impl.SingleObjectCacheImpl

open class FCache(cacheStore: CacheStore) : Cache {
    private val _cacheStore = cacheStore

    private val _cacheInfo = object : CacheInfo {
        override val cacheStore: CacheStore get() = _cacheStore
        override val objectConverter: ObjectConverter get() = CacheConfig.get().objectConverter
        override val exceptionHandler: ExceptionHandler get() = CacheConfig.get().exceptionHandler
    }

    private var _intHandler: CommonCache<Int>? = null
    private var _longHandler: CommonCache<Long>? = null
    private var _floatHandler: CommonCache<Float>? = null
    private var _doubleHandler: CommonCache<Double>? = null
    private var _booleanHandler: CommonCache<Boolean>? = null
    private var _stringHandler: CommonCache<String>? = null
    private var _bytesHandler: CommonCache<ByteArray>? = null

    private var _objectSingle: SingleObjectCacheImpl<*>? = null
    private var _objectMulti: MultiObjectCacheImpl<*>? = null

    //---------- Cache start ----------

    override fun cacheInt(): CommonCache<Int> {
        return _intHandler ?: IntHandler(_cacheInfo).also {
            _intHandler = it
        }
    }

    override fun cacheLong(): CommonCache<Long> {
        return _longHandler ?: LongHandler(_cacheInfo).also {
            _longHandler = it
        }
    }

    override fun cacheFloat(): CommonCache<Float> {
        return _floatHandler ?: FloatHandler(_cacheInfo).also {
            _floatHandler = it
        }
    }

    override fun cacheDouble(): CommonCache<Double> {
        return _doubleHandler ?: DoubleHandler(_cacheInfo).also {
            _doubleHandler = it
        }
    }

    override fun cacheBoolean(): CommonCache<Boolean> {
        return _booleanHandler ?: BooleanHandler(_cacheInfo).also {
            _booleanHandler = it
        }
    }

    override fun cacheString(): CommonCache<String> {
        return _stringHandler ?: StringHandler(_cacheInfo).also {
            _stringHandler = it
        }
    }

    override fun cacheBytes(): CommonCache<ByteArray> {
        return _bytesHandler ?: BytesHandler(_cacheInfo).also {
            _bytesHandler = it
        }
    }

    override fun <T> objectSingle(clazz: Class<T>): SingleObjectCache<T> {
        val cache = _objectSingle
        if (cache?.objectClass == clazz) return (cache as SingleObjectCache<T>)
        return SingleObjectCacheImpl(_cacheInfo, clazz).also {
            _objectSingle = it
        }
    }

    override fun <T> objectMulti(clazz: Class<T>): MultiObjectCache<T> {
        val cache = _objectMulti
        if (cache?.objectClass == clazz) return (cache as MultiObjectCache<T>)
        return MultiObjectCacheImpl(_cacheInfo, clazz).also {
            _objectMulti = it
        }
    }

    //---------- Cache end ----------

    companion object {
        @JvmStatic
        fun instance(): Cache = FCache(CacheConfig.get().cacheStore)

        /** [SingleObjectCache] */
        @JvmStatic
        fun <T> objectSingle(clazz: Class<T>): SingleObjectCache<T> = instance().objectSingle(clazz)

        /** MultiObjectCache */
        @JvmStatic
        fun <T> objectMulti(clazz: Class<T>): MultiObjectCache<T> = instance().objectMulti(clazz)

        /** Int */
        @JvmStatic
        fun cacheInt(): CommonCache<Int> = instance().cacheInt()

        /** Long */
        @JvmStatic
        fun cacheLong(): CommonCache<Long> = instance().cacheLong()

        /** Float */
        @JvmStatic
        fun cacheFloat(): CommonCache<Float> = instance().cacheFloat()

        /** Double */
        @JvmStatic
        fun cacheDouble(): CommonCache<Double> = instance().cacheDouble()

        /** Boolean */
        @JvmStatic
        fun cacheBoolean(): CommonCache<Boolean> = instance().cacheBoolean()

        /** String */
        @JvmStatic
        fun cacheString(): CommonCache<String> = instance().cacheString()

        /** Bytes */
        @JvmStatic
        fun cacheBytes(): CommonCache<ByteArray> = instance().cacheBytes()
    }
}

/**
 * [FCache.instance]
 */
val fCache: Cache get() = FCache.instance()

/** Int */
fun fCacheInt(): CommonCache<Int> = FCache.cacheInt()