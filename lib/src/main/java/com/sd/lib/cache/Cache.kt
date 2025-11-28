package com.sd.lib.cache

interface Cache<T> {
  /** 设置缓存 */
  fun put(key: String, value: T?): Boolean

  /** 获取缓存 */
  fun get(key: String): T?

  /** 删除缓存 */
  fun remove(key: String)

  /** 是否有[key]对应的缓存 */
  fun contains(key: String): Boolean

  /** 所有缓存key */
  fun keys(): List<String>
}