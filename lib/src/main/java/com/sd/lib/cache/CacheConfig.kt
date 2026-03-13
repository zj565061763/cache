package com.sd.lib.cache

import android.annotation.SuppressLint
import android.content.Context
import com.sd.lib.cache.store.CacheStore
import com.sd.lib.cache.store.FileCacheStore
import java.io.File
import java.security.MessageDigest

class CacheConfig private constructor(
  builder: Builder,
  context: Context,
) {
  private val context = context.applicationContext
  private val cacheStoreFactory: CacheStoreFactory
  internal val directory: File by lazy { context.filesDir.resolve("sd.lib.cache") }
  internal val objectConverter: ObjectConverter
  internal val exceptionHandler: ExceptionHandler

  init {
    this.cacheStoreFactory = builder.cacheStoreFactory ?: CacheStoreFactory { FileCacheStore() }
    this.objectConverter = builder.objectConverter ?: DefaultObjectConverter()
    this.exceptionHandler = LibExceptionHandler(builder.exceptionHandler)
  }

  /** 创建仓库 */
  internal fun newCacheStore(group: String, id: String): CacheStore? {
    return libRunCatching {
      cacheStoreFactory.create().also { cacheStore ->
        cacheStore.init(
          context = context,
          directory = directory.resolve(md5(group)).resolve(md5(id)),
        )
      }
    }.getOrNull()
  }

  /**
   * 缓存仓库工厂
   */
  fun interface CacheStoreFactory {
    fun create(): CacheStore
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
    internal var cacheStoreFactory: CacheStoreFactory? = null
      private set

    internal var objectConverter: ObjectConverter? = null
      private set

    internal var exceptionHandler: ExceptionHandler? = null
      private set

    /** 缓存仓库工厂 */
    fun setCacheStoreFactory(factory: CacheStoreFactory?) = apply {
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
    @SuppressLint("StaticFieldLeak")
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

private fun md5(input: String): String {
  val md5Bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
  return buildString {
    for (byte in md5Bytes) {
      val hex = (0xff and byte.toInt()).toString(16)
      if (hex.length == 1) append("0")
      append(hex)
    }
  }
}