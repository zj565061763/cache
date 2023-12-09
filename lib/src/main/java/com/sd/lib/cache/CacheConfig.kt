package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.simple.ObjectConverterImpl
import com.sd.lib.cache.store.UnlimitedDiskCacheStore
import java.io.File

class CacheConfig private constructor(builder: Builder, context: Context) {
    internal val directory: File
    internal val cacheStore: Cache.CacheStore
    internal val objectConverter: Cache.ObjectConverter
    internal val exceptionHandler: Cache.ExceptionHandler

    init {
        directory = builder.directory ?: context.filesDir.resolve("f_cache")
        cacheStore = builder.cacheStore ?: UnlimitedDiskCacheStore()
        objectConverter = builder.objectConverter ?: ObjectConverterImpl()
        exceptionHandler = builder.exceptionHandler ?: Cache.ExceptionHandler { }

        // 初始化仓库
        cacheStore.init(context, directory)
    }

    class Builder {
        internal var directory: File? = null
            private set

        internal var cacheStore: Cache.CacheStore? = null
            private set

        internal var objectConverter: Cache.ObjectConverter? = null
            private set

        internal var exceptionHandler: Cache.ExceptionHandler? = null
            private set

        /**
         * 保存目录
         */
        fun setDirectory(directory: File) = apply {
            this.directory = directory
        }

        /**
         * 仓库ø
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
         */
        fun build(context: Context): CacheConfig {
            return CacheConfig(this, context.applicationContext)
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