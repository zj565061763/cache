package com.sd.lib.cache

import android.annotation.SuppressLint
import android.content.Context
import com.sd.lib.cache.impl.GsonObjectConverter
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.DefaultCacheStore
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class CacheConfig private constructor(builder: Builder, context: Context) {
    private val context: Context
    private val directory: File
    private val cacheStoreClass: Class<out CacheStore>

    internal val objectConverter: Cache.ObjectConverter
    internal val exceptionHandler: Cache.ExceptionHandler

    init {
        this.context = context.applicationContext
        this.directory = builder.directory ?: context.filesDir.resolve("f_cache")
        this.cacheStoreClass = builder.cacheStore ?: DefaultCacheStore::class.java

        this.objectConverter = builder.objectConverter ?: GsonObjectConverter()
        this.exceptionHandler = builder.exceptionHandler ?: Cache.ExceptionHandler { }
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
        require(group.isNotEmpty())
        require(id.isNotEmpty())
        val uid = "${group}:${id}"
        try {
            cacheStore.init(context, directory, uid)
        } catch (e: Throwable) {
            exceptionHandler.notifyException(e)
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
         * 缓存仓库
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
        private val sInitFlag = AtomicBoolean(false)

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sConfig: CacheConfig? = null

        /**
         * 初始化
         */
        @JvmStatic
        fun init(config: CacheConfig) {
            if (sInitFlag.compareAndSet(false, true)) {
                sConfig = config
            }
        }

        internal fun get(): CacheConfig {
            return checkNotNull(sConfig) { "You should call init() before this." }
        }
    }
}