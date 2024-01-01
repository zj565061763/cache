package com.sd.lib.cache

import android.annotation.SuppressLint
import android.content.Context
import com.sd.lib.cache.impl.GsonObjectConverter
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.LimitCacheStore
import com.sd.lib.cache.store.MMKVCacheStore
import com.sd.lib.cache.store.limitCount
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
        return cacheStore.getDeclaredConstructor().newInstance().also { store ->
            if (init) {
                initCacheStore(store, id)
            }
        }
    }

    /**
     * 初始化仓库
     */
    private fun initCacheStore(store: CacheStore, id: String) {
        store.init(context, directory, id)
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
         * 限制个数的仓库，如果[id]相同则返回的是同一个仓库，[limit]以第一次创建为准
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
         * 限制大小的仓库
         * @param id 必须保证唯一性
         */
        private fun limitStore(limit: Int, id: String, type: StoreType): LimitCacheStore {
            val config = get()
            if (type == StoreType.Unlimited) error("Only limited.")

            synchronized(Cache::class.java) {
                sIDTypes[id]?.let { cacheType ->
                    check(cacheType == type) { "ID $id exist with type ${cacheType}." }
                }

                return sLimitStores.getOrPut(id) {
                    val newStore = config.newCacheStore(id = id, init = false)
                    when (type) {
                        StoreType.LimitCount -> newStore.limitCount(limit)
                        else -> error("Only limited.")
                    }.also {
                        config.initCacheStore(it, id)
                        sIDTypes[id] = type
                    }
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

private enum class StoreType {
    Unlimited,
    LimitCount,
}