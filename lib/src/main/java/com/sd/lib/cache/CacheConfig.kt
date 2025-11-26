package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.FileCacheStore
import java.io.File

class CacheConfig private constructor(
  builder: Builder,
  context: Context,
) {
  private val context = context.applicationContext
  private val cacheStoreFactory: () -> CacheStore
  internal val directory: File = context.defaultCacheDir()
  internal val objectConverter: ObjectConverter
  internal val exceptionHandler: ExceptionHandler

  init {
    this.cacheStoreFactory = builder.cacheStoreFactory ?: { FileCacheStore() }
    this.objectConverter = builder.objectConverter ?: DefaultObjectConverter()
    this.exceptionHandler = LibExceptionHandler(builder.exceptionHandler)
  }

  /** 创建仓库 */
  internal fun newCacheStore(group: String, id: String): CacheStore? {
    return libRunCatching {
      cacheStoreFactory().also { cacheStore ->
        cacheStore.init(
          context = context,
          directory = directory,
          group = group,
          id = id,
        )
      }
    }.getOrNull()
  }

  /**
   * 对象转换器，[encode]和[decode]可能存在并发，注意线程安全
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
    internal var cacheStoreFactory: (() -> CacheStore)? = null
      private set

    internal var objectConverter: ObjectConverter? = null
      private set

    internal var exceptionHandler: ExceptionHandler? = null
      private set

    /** 缓存仓库工厂 */
    fun setCacheStoreFactory(factory: (() -> CacheStore)?) = apply {
      this.cacheStoreFactory = factory
    }

    /** 对象转换 */
    fun setObjectConverter(converter: ObjectConverter) = apply {
      this.objectConverter = converter
    }

    /** 异常处理 */
    fun setExceptionHandler(handler: ExceptionHandler) = apply {
      this.exceptionHandler = handler
    }

    fun build(context: Context): CacheConfig {
      return CacheConfig(this, context)
    }
  }

  companion object {
    private var sConfig: CacheConfig? = null

    /** 初始化 */
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

/** 默认缓存目录 */
private fun Context.defaultCacheDir(): File {
  return filesDir.resolve("sd.lib.cache")
}