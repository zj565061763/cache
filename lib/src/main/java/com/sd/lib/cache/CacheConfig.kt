package com.sd.lib.cache

import android.annotation.SuppressLint
import android.content.Context
import com.sd.lib.cache.impl.GsonObjectConverter
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.LimitCacheStore
import com.sd.lib.cache.store.MMKVCacheStore
import com.sd.lib.cache.store.limitByteCacheStore
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import java.io.File

class CacheConfig private constructor(builder: Builder, context: Context) {
    internal val context: Context
    internal val directory: File

    private val cacheStore: Class<out CacheStore>
    internal val objectConverter: Cache.ObjectConverter
    internal val exceptionHandler: Cache.ExceptionHandler

    init {
        this.context = context.applicationContext
        this.directory = builder.directory ?: context.filesDir.resolve("f_cache")

        this.cacheStore = builder.cacheStore ?: MMKVCacheStore::class.java
        this.objectConverter = builder.objectConverter ?: GsonObjectConverter()
        this.exceptionHandler = builder.exceptionHandler ?: Cache.ExceptionHandler { }
    }

    /**
     * 创建新的仓库
     */
    private fun newCacheStore(init: Boolean = true): CacheStore {
        return cacheStore.newInstance().apply {
            if (init) {
                this.init(context, directory)
            }
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
         * 保存目录
         */
        fun setDirectory(directory: File) = apply {
            this.directory = directory
        }

        /**
         * 设置仓库
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

        /**
         * 构建配置
         */
        fun build(context: Context): CacheConfig {
            return CacheConfig(this, context)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sConfig: CacheConfig? = null

        /** 默认仓库 */
        private lateinit var sDefaultStore: CacheStore

        /** 限制大小的仓库 */
        private val sLimitByteStoreHolder: MutableMap<String, LimitCacheStore> = hashMapOf()

        /**
         * 初始化
         */
        @JvmStatic
        fun init(config: CacheConfig) {
            synchronized(Cache::class.java) {
                sConfig?.let { return }
                sConfig = config
                MMKV.initialize(config.context, config.directory.absolutePath, MMKVLogLevel.LevelNone)
                sDefaultStore = config.newCacheStore()
            }
        }

        /**
         * 默认的仓库
         */
        internal fun defaultStore(): CacheStore {
            get()
            return sDefaultStore
        }

        /**
         * 限制大小的仓库，单位Byte
         */
        internal fun limitSizeStore(
            limit: Int,
            directory: File,
        ): CacheStore {
            val key = directory.absolutePath
            if (key == get().directory.absolutePath) error("Default directory is unlimited ${key}.")

            val store = sLimitByteStoreHolder.getOrPut(key) {
                limitByteCacheStore(
                    limit = limit,
                    store = get().newCacheStore(init = false),
                ).apply {
                    this.init(get().context, directory)
                }
            }
            return store.also {
                it.limit(limit)
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