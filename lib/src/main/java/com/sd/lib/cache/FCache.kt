package com.sd.lib.cache

import com.sd.lib.cache.Cache.*
import com.sd.lib.cache.handler.impl.*
import com.sd.lib.cache.simple.SimpleMultiObjectCache
import com.sd.lib.cache.simple.SimpleObjectCache

open class FCache(cacheStore: CacheStore) : Cache {
    private val _cacheStore = cacheStore

    private var _objectConverter: ObjectConverter? = null
    private var _exceptionHandler: ExceptionHandler? = null

    private val _cacheInfo = object : CacheInfo {
        override val cacheStore: CacheStore get() = _cacheStore
        override val objectConverter: ObjectConverter get() = _objectConverter ?: CacheConfig.get().objectConverter
        override val exceptionHandler: ExceptionHandler get() = _exceptionHandler ?: CacheConfig.get().exceptionHandler
    }

    private val _integerHandler by lazy { IntegerHandler(_cacheInfo) }
    private val _longHandler by lazy { LongHandler(_cacheInfo) }
    private val _floatHandler by lazy { FloatHandler(_cacheInfo) }
    private val _doubleHandler by lazy { DoubleHandler(_cacheInfo) }
    private val _booleanHandler by lazy { BooleanHandler(_cacheInfo) }
    private val _stringHandler by lazy { StringHandler(_cacheInfo) }
    private val _bytesHandler by lazy { BytesHandler(_cacheInfo) }

    private val _objectCache by lazy { SimpleObjectCache(_cacheInfo) }
    private var _multiObjectCache: SimpleMultiObjectCache<*>? = null

    override fun setObjectConverter(converter: ObjectConverter?): Cache {
        _objectConverter = converter
        return this
    }

    override fun setExceptionHandler(handler: ExceptionHandler?): Cache {
        _exceptionHandler = handler
        return this
    }

    //---------- Cache start ----------

    override fun cacheInteger(): CommonCache<Int> {
        return _integerHandler
    }

    override fun cacheLong(): CommonCache<Long> {
        return _longHandler
    }

    override fun cacheFloat(): CommonCache<Float> {
        return _floatHandler
    }

    override fun cacheDouble(): CommonCache<Double> {
        return _doubleHandler
    }

    override fun cacheBoolean(): CommonCache<Boolean> {
        return _booleanHandler
    }

    override fun cacheString(): CommonCache<String> {
        return _stringHandler
    }

    override fun cacheBytes(): CommonCache<ByteArray> {
        return _bytesHandler
    }

    override fun cacheObject(): ObjectCache {
        return _objectCache
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> cacheMultiObject(clazz: Class<T>): MultiObjectCache<T> {
        val cache = _multiObjectCache
        if (cache != null && cache.objectClass == clazz) return (cache as MultiObjectCache<T>)
        return SimpleMultiObjectCache(_cacheInfo, clazz).also {
            _multiObjectCache = it
        }
    }

    //---------- Cache end ----------

    companion object {
        /**
         * 创建并返回一个本地磁盘缓存对象，
         * 默认使用内部存储目录"/data/包名/files/f_disk_cache"，可以在初始化的时候设置[CacheConfig.Builder.setCacheStore]
         */
        @JvmStatic
        fun disk(): Cache {
            val cacheStore = CacheConfig.get().cacheStore
            return FCache(cacheStore)
        }
    }
}