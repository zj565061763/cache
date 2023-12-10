package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.impl.GsonObjectConverter
import com.sd.lib.cache.store.CacheStore
import java.io.File

class CacheConfig private constructor(builder: Builder, context: Context) {
    internal val cacheStore: CacheStore
    internal val objectConverter: Cache.ObjectConverter
    internal val exceptionHandler: Cache.ExceptionHandler

    init {
        val directory = builder.directory ?: context.filesDir.resolve("f_cache")
        cacheStore = CacheStore.init(context, directory)
        objectConverter = builder.objectConverter ?: GsonObjectConverter()
        exceptionHandler = builder.exceptionHandler ?: Cache.ExceptionHandler { }
    }

    class Builder {
        internal var directory: File? = null
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
                return sConfig ?: error("You should call init() before this.")
            }
        }
    }
}