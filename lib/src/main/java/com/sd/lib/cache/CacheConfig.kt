package com.sd.lib.cache

import android.annotation.SuppressLint
import android.content.Context
import com.sd.lib.cache.impl.GsonObjectConverter
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.FileCacheStore
import java.io.File

@SuppressLint("StaticFieldLeak")
class CacheConfig private constructor(
    builder: Builder,
    context: Context,
) {
    private val context = context.applicationContext

    private val directory: File
    private val cacheStoreClass: Class<out CacheStore>

    internal val objectConverter: Cache.ObjectConverter
    internal val exceptionHandler: Cache.ExceptionHandler

    init {
        this.directory = builder.directory ?: context.filesDir.resolve("f_cache")
        this.cacheStoreClass = builder.cacheStore ?: FileCacheStore::class.java

        this.objectConverter = builder.objectConverter ?: GsonObjectConverter()
        this.exceptionHandler = LibExceptionHandler(builder.exceptionHandler)
    }

    /**
     * 创建仓库
     */
    internal fun newCacheStore(): CacheStore {
        return cacheStoreClass.getDeclaredConstructor().newInstance()
    }

    /**
     * 初始化仓库
     */
    internal fun initCacheStore(cacheStore: CacheStore, group: String, id: String) {
        if (group.isEmpty()) libError("group is empty.")
        if (id.isEmpty()) libError("id is empty.")
        try {
            cacheStore.init(
                context = context,
                directory = directory,
                group = group,
                id = id,
            )
        } catch (e: Throwable) {
            libNotifyException(e)
        }
    }

    class Builder {
        internal var directory: File? = null
            private set

        internal var cacheStore: Class<out CacheStore>? = null
            private set

        internal var objectConverter: Cache.ObjectConverter? = null
            private set

        internal var exceptionHandler: Cache.ExceptionHandler? = null
            private set

        /**
         * 缓存目录
         */
        fun setDirectory(directory: File) = apply {
            this.directory = directory
        }

        /**
         * 缓存仓库，[store]必须有无参构造方法用于反射创建对象
         */
        fun setCacheStore(store: Class<out CacheStore>) = apply {
            this.cacheStore = store
        }

        /**
         * 对象转换
         */
        fun setObjectConverter(converter: Cache.ObjectConverter) = apply {
            this.objectConverter = converter
        }

        /**
         * 异常处理
         */
        fun setExceptionHandler(handler: Cache.ExceptionHandler) = apply {
            this.exceptionHandler = handler
        }

        fun build(context: Context): CacheConfig {
            return CacheConfig(this, context)
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
            synchronized(this@Companion) {
                if (sConfig == null) {
                    sConfig = config
                } else {
                    error("CacheConfig has been initialized.")
                }
            }
        }

        internal fun get(): CacheConfig {
            return sConfig ?: synchronized(this@Companion) {
                checkNotNull(sConfig) { "CacheConfig.init() should be called before this." }
            }
        }
    }
}