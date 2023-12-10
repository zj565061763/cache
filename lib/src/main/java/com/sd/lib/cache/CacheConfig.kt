package com.sd.lib.cache

import android.annotation.SuppressLint
import android.content.Context
import com.sd.lib.cache.impl.GsonObjectConverter
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.LimitCacheStore
import com.sd.lib.cache.store.MMKVCacheStore
import com.sd.lib.cache.store.limitByteCacheStore
import com.sd.lib.cache.store.limitCountCacheStore
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
    private fun newCacheStore(id: String, init: Boolean): CacheStore {
        return cacheStore.getDeclaredConstructor().newInstance().apply {
            if (init) {
                this.init(context, directory, id)
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
        private const val DefaultID = "com.sd.lib.cache.id.default"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sConfig: CacheConfig? = null

        /** 默认仓库 */
        private lateinit var sDefaultStore: CacheStore

        /** ID对应的仓库类型 */
        private val sIDTypes: MutableMap<String, StoreType> = hashMapOf()

        /** 限制大小的仓库 */
        private val sLimitStores: MutableMap<String, LimitCacheStore> = hashMapOf()

        /**
         * 初始化
         */
        @JvmStatic
        fun init(config: CacheConfig) {
            synchronized(Cache::class.java) {
                sConfig?.let { return }
                sConfig = config
                MMKV.initialize(config.context, config.directory.absolutePath, MMKVLogLevel.LevelNone)
                sDefaultStore = config.newCacheStore(id = DefaultID, init = true).also {
                    sIDTypes[DefaultID] = StoreType.Unlimited
                }
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
         * 限制个数的仓库
         * @param id 必须保证唯一性
         */
        internal fun limitCountStore(limit: Int, id: String): CacheStore {
            return limitStore(
                limit = limit,
                id = id,
                type = StoreType.LimitCount,
            )
        }

        /**
         * 限制大小的仓库，单位Byte
         * @param id 必须保证唯一性
         */
        internal fun limitByteStore(limit: Int, id: String): CacheStore {
            return limitStore(
                limit = limit,
                id = id,
                type = StoreType.LimitByte,
            )
        }

        /**
         * 限制大小的仓库
         * @param id 必须保证唯一性
         */
        private fun limitStore(limit: Int, id: String, type: StoreType): CacheStore {
            val config = get()
            if (type == StoreType.Unlimited) error("Only limited.")

            synchronized(Cache::class.java) {
                sIDTypes[id]?.let { cache ->
                    check(cache == type) { "ID $id exist with type ${cache}." }
                }

                val limitStore = sLimitStores.getOrPut(id) {
                    when (type) {
                        StoreType.LimitCount -> limitCountCacheStore(
                            limit = limit,
                            store = config.newCacheStore(id = id, init = false),
                        )

                        StoreType.LimitByte -> limitByteCacheStore(
                            limit = limit,
                            store = config.newCacheStore(id = id, init = false),
                        )

                        else -> error("Only limited.")
                    }.also {
                        it.init(
                            context = config.context,
                            directory = config.directory,
                            id = id,
                        )
                        sIDTypes[id] = type
                    }
                }

                return limitStore.also {
                    it.limit(limit)
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

internal enum class StoreType {
    Unlimited,
    LimitCount,
    LimitByte,
}