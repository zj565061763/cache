package com.sd.lib.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface SingleCacheKtx<T> {
  /** 缓存 */
  fun flow(): Flow<T>

  /**
   * 更新缓存，[block]在[Dispatchers.IO]上面执行，如果[block]返回null则删除缓存
   * @return true-更新成功；false-更新失败
   */
  suspend fun update(block: suspend (T) -> T?): Boolean
}

fun <T> CacheKtx<T>.asSingleCacheKtx(
  /** 缓存key */
  key: String = FCache.DEFAULT_SINGLE_CACHE_KEY,
  /** 默认缓存，在[Dispatchers.IO]上面执行 */
  getDefault: () -> T,
): SingleCacheKtx<T> {
  return SingleCacheKtxImpl(
    cache = this,
    key = key,
    getDefault = getDefault,
  )
}

private class SingleCacheKtxImpl<T>(
  private val cache: CacheKtx<T>,
  private val key: String,
  private val getDefault: () -> T,
) : SingleCacheKtx<T> {
  private val _flow = cache.flowOf(key)
    .map { it ?: getDefault() }
    .distinctUntilChanged()
    .flowOn(Dispatchers.IO)

  override fun flow(): Flow<T> = _flow

  override suspend fun update(block: suspend (T) -> T?): Boolean {
    return cache.edit {
      val oldCache = get(key) ?: getDefault()
      val newCache = block(oldCache)
      if (newCache != null) {
        put(key, newCache)
      } else {
        remove(key)
        true
      }
    }
  }
}