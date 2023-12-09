package com.sd.lib.cache

import com.sd.lib.cache.Cache.CacheStore
import com.sd.lib.cache.Cache.CommonCache
import com.sd.lib.cache.Cache.ExceptionHandler
import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.Cache.ObjectCache
import com.sd.lib.cache.Cache.ObjectConverter
import com.sd.lib.cache.Cache.SingleObjectCache
import com.sd.lib.cache.handler.impl.BooleanHandler
import com.sd.lib.cache.handler.impl.BytesHandler
import com.sd.lib.cache.handler.impl.DoubleHandler
import com.sd.lib.cache.handler.impl.FloatHandler
import com.sd.lib.cache.handler.impl.IntHandler
import com.sd.lib.cache.handler.impl.LongHandler
import com.sd.lib.cache.handler.impl.StringHandler
import com.sd.lib.cache.simple.MultiObjectCacheImpl
import com.sd.lib.cache.simple.SimpleObjectCache
import com.sd.lib.cache.simple.SingleObjectCacheImpl

open class FCache(cacheStore: CacheStore) : Cache {
    private val _cacheStore = cacheStore

    private var _objectConverter: ObjectConverter? = null
    private var _exceptionHandler: ExceptionHandler? = null

    private val _cacheInfo = object : CacheInfo {
        override val cacheStore: CacheStore get() = _cacheStore
        override val objectConverter: ObjectConverter get() = _objectConverter ?: CacheConfig.get().objectConverter
        override val exceptionHandler: ExceptionHandler get() = _exceptionHandler ?: CacheConfig.get().exceptionHandler
    }

    private var _intHandler: CommonCache<Int>? = null
    private var _longHandler: CommonCache<Long>? = null
    private var _floatHandler: CommonCache<Float>? = null
    private var _doubleHandler: CommonCache<Double>? = null
    private var _booleanHandler: CommonCache<Boolean>? = null
    private var _stringHandler: CommonCache<String>? = null
    private var _bytesHandler: CommonCache<ByteArray>? = null

    private var _objectCache: ObjectCache? = null

    private var _objectSingle: SingleObjectCacheImpl<*>? = null
    private var _objectMulti: MultiObjectCacheImpl<*>? = null

    override fun setObjectConverter(converter: ObjectConverter?): Cache {
        _objectConverter = converter
        return this
    }

    override fun setExceptionHandler(handler: ExceptionHandler?): Cache {
        _exceptionHandler = handler
        return this
    }

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

    override fun cacheObject(): ObjectCache {
        return _objectCache ?: SimpleObjectCache(_cacheInfo).also {
            _objectCache = it
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
        /**
         * 默认使用内部存储目录"/data/包名/files/f_disk_cache"，可以在初始化的时候设置[CacheConfig.Builder.setCacheStore]
         */
        @JvmStatic
        fun disk(): Cache {
            val cacheStore = CacheConfig.get().cacheStore
            return FCache(cacheStore)
        }
    }
}

/**
 * [FCache.disk]
 */
val fCache: Cache get() = FCache.disk()