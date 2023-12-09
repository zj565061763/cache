package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.simple.GsonObjectConverter
import com.sd.lib.cache.store.UnlimitedDiskCacheStore

class CacheConfig private constructor(builder: Builder) {
    internal val cacheStore: Cache.CacheStore
    internal val objectConverter: Cache.ObjectConverter
    internal val exceptionHandler: Cache.ExceptionHandler

    init {
        val context = builder.context
        cacheStore = builder.cacheStore ?: UnlimitedDiskCacheStore(context.filesDir.resolve("f_disk_cache"))
        objectConverter = builder.objectConverter ?: GsonObjectConverter()
        exceptionHandler = builder.exceptionHandler ?: Cache.ExceptionHandler { }
    }

    class Builder {
        internal lateinit var context: Context
            private set

        internal var cacheStore: Cache.CacheStore? = null
            private set

        internal var objectConverter: Cache.ObjectConverter? = null
            private set

        internal var exceptionHandler: Cache.ExceptionHandler? = null
            private set

        /**
         * 缓存库
         */
        fun setCacheStore(store: Cache.CacheStore?) = apply {
            this.cacheStore = store
        }

        /**
         * 对象转换
         */
        fun setObjectConverter(converter: Cache.ObjectConverter?) = apply {
            this.objectConverter = converter
        }

        /**
         * 异常处理
         */
        fun setExceptionHandler(handler: Cache.ExceptionHandler?) = apply {
            this.exceptionHandler = handler
        }

        fun build(context: Context): CacheConfig {
            this.context = context.applicationContext
            return CacheConfig(this)
        }
    }

    companion object {
        @Volatile
        private var sConfig: CacheConfig? = null

        /**
         * 初始化
         */
        @JvmStatic
        fun init(config: CacheConfig) {
            synchronized(Cache::class.java) {
                if (sConfig == null) {
                    sConfig = config
                }
            }
        }

        internal fun get(): CacheConfig {
            val config = sConfig
            if (config != null) return config
            synchronized(Cache::class.java) {
                return sConfig ?: error("CacheConfig has not been init")
            }
        }
    }
}