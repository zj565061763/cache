package com.sd.lib.cache

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
import com.sd.lib.cache.store.CacheStore

class FCache private constructor(store: CacheStore) : Cache {

    private var _cInt: CommonCache<Int>? = null
    private var _cLong: CommonCache<Long>? = null
    private var _cFloat: CommonCache<Float>? = null
    private var _cDouble: CommonCache<Double>? = null
    private var _cBoolean: CommonCache<Boolean>? = null
    private var _cString: CommonCache<String>? = null
    private var _cBytes: CommonCache<ByteArray>? = null

    private var _cObject: SingleObjectCacheImpl<*>? = null
    private var _cObjects: MultiObjectCacheImpl<*>? = null

    private val _cacheInfo = object : CacheInfo {
        override val cacheStore: CacheStore get() = store
        override val objectConverter: ObjectConverter get() = CacheConfig.get().objectConverter
        override val exceptionHandler: ExceptionHandler get() = CacheConfig.get().exceptionHandler
    }

    //---------- Cache start ----------

    override fun cInt(): CommonCache<Int> {
        return _cInt ?: IntHandler(_cacheInfo).also {
            _cInt = it
        }
    }

    override fun cLong(): CommonCache<Long> {
        return _cLong ?: LongHandler(_cacheInfo).also {
            _cLong = it
        }
    }

    override fun cFloat(): CommonCache<Float> {
        return _cFloat ?: FloatHandler(_cacheInfo).also {
            _cFloat = it
        }
    }

    override fun cDouble(): CommonCache<Double> {
        return _cDouble ?: DoubleHandler(_cacheInfo).also {
            _cDouble = it
        }
    }

    override fun cBoolean(): CommonCache<Boolean> {
        return _cBoolean ?: BooleanHandler(_cacheInfo).also {
            _cBoolean = it
        }
    }

    override fun cString(): CommonCache<String> {
        return _cString ?: StringHandler(_cacheInfo).also {
            _cString = it
        }
    }

    override fun cBytes(): CommonCache<ByteArray> {
        return _cBytes ?: BytesHandler(_cacheInfo).also {
            _cBytes = it
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> cObject(clazz: Class<T>): SingleObjectCache<T> {
        val cache = _cObject
        if (cache?.objectClass == clazz) return (cache as SingleObjectCache<T>)
        return SingleObjectCacheImpl(_cacheInfo, clazz).also {
            _cObject = it
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> cObjects(clazz: Class<T>): MultiObjectCache<T> {
        val cache = _cObjects
        if (cache?.objectClass == clazz) return (cache as MultiObjectCache<T>)
        return MultiObjectCacheImpl(_cacheInfo, clazz).also {
            _cObjects = it
        }
    }

    //---------- Cache end ----------

    companion object {
        private val sDefault by lazy {
            val store = CacheConfig.defaultStore()
            FCache(store)
        }

        /**
         * 默认无限制缓存
         */
        @JvmStatic
        fun get(): Cache = sDefault

        /**
         * 限制个数的缓存
         * @param id 必须保证唯一性
         */
        @JvmStatic
        fun limitCount(limit: Int, id: String): Cache {
            val store = CacheConfig.limitCountStore(limit, id)
            return FCache(store)
        }
    }
}

val fCache: Cache get() = FCache.get()

inline fun <reified T> Cache.cacheObject(): SingleObjectCache<T> {
    return this.cObject(T::class.java)
}

inline fun <reified T> Cache.cacheObjects(): MultiObjectCache<T> {
    return this.cObjects(T::class.java)
}