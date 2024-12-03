package com.sd.lib.cache

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import com.sd.lib.cache.impl.DefaultObjectConverter
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.FileCacheStore
import java.io.File

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
        this.directory = builder.directory ?: context.defaultCacheDir()
        this.cacheStoreClass = builder.cacheStore ?: FileCacheStore::class.java

        this.objectConverter = builder.objectConverter ?: DefaultObjectConverter()
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
        @SuppressLint("StaticFieldLeak")
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

inline fun CacheConfig.Companion.init(
    context: Context,
    block: CacheConfig.Builder.() -> Unit = {},
) {
    init(
        CacheConfig.Builder()
            .apply(block)
            .build(context)
    )
}

private fun Context.defaultCacheDir(): File {
    @SuppressLint("SdCardPath")
    val dir = (filesDir ?: File("/data/data/${packageName}/files"))
        .resolve("sd.lib.cache")

    val process = currentProcess()
    return if (!process.isNullOrBlank() && process != packageName) {
        dir.resolve(process)
    } else {
        dir
    }
}

private fun Context.currentProcess(): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Application.getProcessName()
    } else {
        runCatching {
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processes = manager.runningAppProcesses
            if (processes.isNullOrEmpty()) {
                null
            } else {
                val pid = Process.myPid()
                processes.find { it.pid == pid }?.processName
            }
        }.getOrNull()
    }
}