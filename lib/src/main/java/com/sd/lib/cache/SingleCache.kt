package com.sd.lib.cache

interface SingleCache<T> {
  /** 设置缓存 */
  fun put(value: T?): Boolean

  /** 获取缓存 */
  fun get(): T?

  /** 删除缓存 */
  fun remove()

  /** 是否有缓存 */
  fun contains(): Boolean
}

fun <T> Cache<T>.asSingleCache(
  key: String = FCache.DEFAULT_SINGLE_CACHE_KEY,
): SingleCache<T> {
  return object : SingleCache<T> {
    override fun put(value: T?): Boolean = this@asSingleCache.put(key, value)
    override fun get(): T? = this@asSingleCache.get(key)
    override fun remove() = this@asSingleCache.remove(key)
    override fun contains(): Boolean = this@asSingleCache.contains(key)
  }
}