package com.sd.lib.cache.store

import android.content.Context
import java.io.File

/**
 * 缓存仓库
 */
interface CacheStore {
  /** 初始化 */
  @Throws(Throwable::class)
  fun init(context: Context, directory: File)

  /** 设置缓存 */
  @Throws(Throwable::class)
  fun putCache(key: String, value: ByteArray)

  /** 获取缓存 */
  @Throws(Throwable::class)
  fun getCache(key: String): ByteArray?

  /** 删除缓存 */
  @Throws(Throwable::class)
  fun removeCache(key: String)

  /** 是否有[key]对应的缓存 */
  @Throws(Throwable::class)
  fun containsCache(key: String): Boolean

  /** 返回所有的Key */
  @Throws(Throwable::class)
  fun keys(): List<String>

  /** 关闭 */
  @Throws(Throwable::class)
  fun close()

  /** 缓存变化回调 */
  fun setCacheChangeCallback(callback: CacheChangeCallback)

  /** 缓存变化回调 */
  interface CacheChangeCallback {
    /** [cacheKey]对应的缓存被创建 */
    fun onCreate(cacheKey: String)

    /** [cacheKey]对应的缓存被修改 */
    fun onModify(cacheKey: String)

    /** [cacheKey]对应的缓存被删除 */
    fun onRemove(cacheKey: String)
  }
}