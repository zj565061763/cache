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

  /** 所有缓存key */
  @Throws(Throwable::class)
  fun keys(): List<String>

  /** 销毁 */
  @Throws(Throwable::class)
  fun destroy()

  /** 缓存变化回调 */
  fun setCacheChangeCallback(callback: CacheChangeCallback)

  /** 缓存变化回调 */
  interface CacheChangeCallback {
    /** [key]对应的缓存被删除 */
    fun onRemove(key: String)

    /** [key]对应的缓存被写入 */
    fun onModify(key: String)
  }
}