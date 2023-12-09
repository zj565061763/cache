package com.sd.lib.cache

import com.sd.lib.cache.simple.ObjectConverterImpl
import com.sd.lib.cache.store.UnlimitedDiskCacheStore
import java.io.File

class CacheConfig private constructor(builder: Builder) {
    internal val directory: File
    internal val cacheStore: Cache.CacheStore
    internal val objectConverter: Cache.ObjectConverter
    internal val exceptionHandler: Cache.ExceptionHandler

    init {
        directory = builder.directory
        cacheStore = builder.cacheStore ?: UnlimitedDiskCacheStore(builder.directory)
        objectConverter = builder.objectConverter ?: ObjectConverterImpl()
        exceptionHandler = builder.exceptionHandler ?: Cache.ExceptionHandler { }
    }

    class Builder {
        internal lateinit var directory: File
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

        /**
         * 构建配置
         * @param directory 保存目录
         */
        fun build(directory: File): CacheConfig {
            this.directory = directory.also {
                if (it.isFile) error("directory is file.")
            }
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
            sConfig?.let { return it }
            synchronized(Cache::class.java) {
                return sConfig ?: error("CacheConfig has not been init")
            }
        }
    }
}