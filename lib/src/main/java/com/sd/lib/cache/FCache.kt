package com.sd.lib.cache

import com.sd.lib.cache.Cache.CommonCache
import com.sd.lib.cache.Cache.ExceptionHandler
import com.sd.lib.cache.Cache.MultiObjectCache
import com.sd.lib.cache.Cache.ObjectConverter
import com.sd.lib.cache.Cache.SingleObjectCache
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
import com.sd.lib.cache.store.limitCount

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
        private const val DefaultID = "com.sd.lib.cache.id.default"
        private const val DefaultMemoryID = "${DefaultID}.memory"

        private val sDefaultCache: Cache by lazy { unlimited(DefaultID) }
        private val sDefaultMemoryCache: Cache by lazy { unlimitedMemory(DefaultMemoryID) }

        /**
         * 默认无限制
         */
        @JvmStatic
        fun get(): Cache = sDefaultCache

        /**
         * 默认内存无限制
         */
        @JvmStatic
        fun getMemory(): Cache = sDefaultMemoryCache

        /**
         * 返回[id]对应的无限制缓存
         */
        @JvmStatic
        fun unlimited(id: String): Cache {
            val store = CacheConfig.getOrPutStore(id, StoreType.Unlimited) {
                it.newStore()
            }
            return FCache(store)
        }

        /**
         * 返回[id]对应的无限制内存缓存
         */
        @JvmStatic
        fun unlimitedMemory(id: String): Cache {
            val store = CacheConfig.getOrPutStore(id, StoreType.UnlimitedMemory) {
                it.newMemoryStore()
            }
            return FCache(store)
        }

        /**
         * 限制个数，如果[id]相同则返回的是同一个缓存，[limit]以第一次创建为准
         * @param id 必须保证唯一性
         */
        @JvmStatic
        fun limitCount(limit: Int, id: String): Cache {
            val store = CacheConfig.getOrPutStore(id, StoreType.LimitCount) {
                it.newStore().limitCount(limit)
            }
            return FCache(store)
        }

        /**
         * 内存限制个数，如果[id]相同则返回的是同一个缓存，[limit]以第一次创建为准
         * @param id 必须保证唯一性
         */
        @JvmStatic
        fun limitCountMemory(limit: Int, id: String): Cache {
            val store = CacheConfig.getOrPutStore(id, StoreType.LimitCountMemory) {
                it.newMemoryStore().limitCount(limit)
            }
            return FCache(store)
        }
    }
}