package com.sd.lib.cache

import android.annotation.SuppressLint
import android.content.Context
import com.sd.lib.cache.impl.GsonObjectConverter
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.MMKVCacheStore
import com.sd.lib.cache.store.MemoryCacheStore
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
     * 创建仓库
     */
    internal fun newStore(): CacheStore {
        return cacheStore.getDeclaredConstructor().newInstance()
    }

    /**
     * 创建内存仓库
     */
    internal fun newMemoryStore(): CacheStore {
        return MemoryCacheStore()
    }

    /**
     * 初始化仓库
     */
    private fun initStore(store: CacheStore, id: String) {
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
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sConfig: CacheConfig? = null

        private val sHolder: MutableMap<String, StoreHolder> = hashMapOf()

        /**
         * 初始化
         */
        @JvmStatic
        fun init(config: CacheConfig) {
            synchronized(Cache::class.java) {
                if (sConfig == null) {
                    sConfig = config
                    MMKV.initialize(config.context, config.directory.absolutePath, MMKVLogLevel.LevelNone)
                }
            }
        }

        internal fun get(): CacheConfig {
            sConfig?.let { return it }
            synchronized(Cache::class.java) {
                return sConfig ?: error("You should call init() before this.")
            }
        }

        /**
         * 获取仓库
         * @param id 必须保证唯一性
         */
        internal fun getStore(
            id: String,
            type: StoreType,
            factory: (CacheConfig) -> CacheStore,
        ): CacheStore {
            val config = get()
            synchronized(Cache::class.java) {
                sHolder[id]?.let { holder ->
                    check(holder.type == type) { "ID $id exist with type ${holder.type}." }
                    return holder.store
                }
                return factory(config).also { store ->
                    sHolder[id] = StoreHolder(type, store)
                    config.initStore(store, id)
                }
            }
        }
    }
}

private data class StoreHolder(
    val type: StoreType,
    val store: CacheStore,
)

internal enum class StoreType {
    Unlimited,
    LimitCount,

    UnlimitedMemory,
    LimitCountMemory,
}