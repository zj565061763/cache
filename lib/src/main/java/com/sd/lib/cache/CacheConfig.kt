package com.sd.lib.cache

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.FileCacheStore
import java.io.File

class CacheConfig private constructor(
    builder: Builder,
    context: Context,
) {
    private val context = context.applicationContext
    private val directory: File
    private val cacheStore: Class<out CacheStore>

    internal val objectConverter: ObjectConverter
    internal val exceptionHandler: ExceptionHandler

    init {
        this.directory = builder.directory ?: context.defaultCacheDir()
        this.cacheStore = builder.cacheStore ?: FileCacheStore::class.java

        this.objectConverter = builder.objectConverter ?: DefaultObjectConverter()
        this.exceptionHandler = LibExceptionHandler(builder.exceptionHandler)
    }

    /** 创建仓库 */
    internal fun newCacheStore(): CacheStore {
        return cacheStore.getDeclaredConstructor().newInstance()
    }

    /** 初始化仓库 */
    internal fun initCacheStore(cacheStore: CacheStore, group: String, id: String): Boolean {
        return try {
            cacheStore.init(
                context = context,
                directory = directory,
                group = group,
                id = id,
            )
            true
        } catch (e: Throwable) {
            libNotifyException(e)
            false
        }
    }

    /**
     * 对象转换器
     */
    interface ObjectConverter {
        /** 编码 */
        @Throws(Throwable::class)
        fun <T> encode(value: T, clazz: Class<T>): ByteArray

        /** 解码 */
        @Throws(Throwable::class)
        fun <T> decode(bytes: ByteArray, clazz: Class<T>): T
    }

    /**
     * 异常处理类
     */
    fun interface ExceptionHandler {
        fun onException(error: Throwable)
    }

    class Builder {
        internal var directory: File? = null
            private set

        internal var cacheStore: Class<out CacheStore>? = null
            private set

        internal var objectConverter: ObjectConverter? = null
            private set

        internal var exceptionHandler: ExceptionHandler? = null
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
        fun setObjectConverter(converter: ObjectConverter) = apply {
            this.objectConverter = converter
        }

        /**
         * 异常处理
         */
        fun setExceptionHandler(handler: ExceptionHandler) = apply {
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
    val filesDir = filesDir ?: File("/data/data/${packageName}/files")
    val dir = filesDir.resolve("sd.lib.cache")

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