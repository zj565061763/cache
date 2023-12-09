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

open class FCache(cacheStore: CacheStore) : Cache, CacheInfo {
    private val _cacheStore = cacheStore

    private var _intHandler: CommonCache<Int>? = null
    private var _longHandler: CommonCache<Long>? = null
    private var _floatHandler: CommonCache<Float>? = null
    private var _doubleHandler: CommonCache<Double>? = null
    private var _booleanHandler: CommonCache<Boolean>? = null
    private var _stringHandler: CommonCache<String>? = null
    private var _bytesHandler: CommonCache<ByteArray>? = null

    private var _objectSingle: SingleObjectCacheImpl<*>? = null
    private var _objectMulti: MultiObjectCacheImpl<*>? = null

    override val cacheStore: CacheStore get() = _cacheStore
    override val objectConverter: ObjectConverter get() = CacheConfig.get().objectConverter
    override val exceptionHandler: ExceptionHandler get() = CacheConfig.get().exceptionHandler

    //---------- Cache start ----------

    override fun cacheInt(): CommonCache<Int> {
        return _intHandler ?: IntHandler(this@FCache).also {
            _intHandler = it
        }
    }

    override fun cacheLong(): CommonCache<Long> {
        return _longHandler ?: LongHandler(this@FCache).also {
            _longHandler = it
        }
    }

    override fun cacheFloat(): CommonCache<Float> {
        return _floatHandler ?: FloatHandler(this@FCache).also {
            _floatHandler = it
        }
    }

    override fun cacheDouble(): CommonCache<Double> {
        return _doubleHandler ?: DoubleHandler(this@FCache).also {
            _doubleHandler = it
        }
    }

    override fun cacheBoolean(): CommonCache<Boolean> {
        return _booleanHandler ?: BooleanHandler(this@FCache).also {
            _booleanHandler = it
        }
    }

    override fun cacheString(): CommonCache<String> {
        return _stringHandler ?: StringHandler(this@FCache).also {
            _stringHandler = it
        }
    }

    override fun cacheBytes(): CommonCache<ByteArray> {
        return _bytesHandler ?: BytesHandler(this@FCache).also {
            _bytesHandler = it
        }
    }

    override fun <T> objectSingle(clazz: Class<T>): SingleObjectCache<T> {
        val cache = _objectSingle
        if (cache?.objectClass == clazz) return (cache as SingleObjectCache<T>)
        return SingleObjectCacheImpl(this@FCache, clazz).also {
            _objectSingle = it
        }
    }

    override fun <T> objectMulti(clazz: Class<T>): MultiObjectCache<T> {
        val cache = _objectMulti
        if (cache?.objectClass == clazz) return (cache as MultiObjectCache<T>)
        return MultiObjectCacheImpl(this@FCache, clazz).also {
            _objectMulti = it
        }
    }

    //---------- Cache end ----------

    companion object {
        @JvmStatic
        fun instance(): Cache = FCache(CacheConfig.get().cacheStore)
    }
}

/** [SingleObjectCache] */
inline fun <reified T> fCacheObject(): SingleObjectCache<T> = FCache.instance().objectSingle(T::class.java)

/** [MultiObjectCache] */
inline fun <reified T> fCacheObjectMulti(): MultiObjectCache<T> = FCache.instance().objectMulti(T::class.java)

/** Int */
val fCacheInt: CommonCache<Int> get() = FCache.instance().cacheInt()

/** Long */
val fCacheLong: CommonCache<Long> get() = FCache.instance().cacheLong()

/** Float */
val fCacheFloat: CommonCache<Float> get() = FCache.instance().cacheFloat()

/** Double */
val fCacheDouble: CommonCache<Double> get() = FCache.instance().cacheDouble()

/** Boolean */
val fCacheBoolean: CommonCache<Boolean> get() = FCache.instance().cacheBoolean()

/** String */
val fCacheString: CommonCache<String> get() = FCache.instance().cacheString()

/** Bytes */
val fCacheBytes: CommonCache<ByteArray> get() = FCache.instance().cacheBytes()