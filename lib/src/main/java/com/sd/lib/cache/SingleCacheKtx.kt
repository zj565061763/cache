package com.sd.lib.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface SingleCacheKtx<T> {
  /** 缓存 */
  fun flow(): Flow<T>

  /** 更新缓存，[block]在[Dispatchers.IO]上面执行，如果[block]返回null则删除缓存 */
  suspend fun update(block: suspend (T) -> T?)
}

fun <T> CacheKtx<T>.asSingleCacheKtx(
  /** 缓存key */
  key: String = FCache.DEFAULT_SINGLE_CACHE_KEY,
  /** 默认缓存，在[Dispatchers.IO]上面执行 */
  defaultCache: () -> T,
): SingleCacheKtx<T> {
  return SingleCacheKtxImpl(
    cache = this,
    key = key,
    defaultCache = defaultCache,
  )
}

private class SingleCacheKtxImpl<T>(
  private val cache: CacheKtx<T>,
  private val key: String,
  private val defaultCache: () -> T,
) : SingleCacheKtx<T> {
  private val _flow = cache.flowOf(key)
    .map { it ?: defaultCache() }
    .distinctUntilChanged()
    .flowOn(Dispatchers.IO)

  override fun flow(): Flow<T> = _flow

  override suspend fun update(block: suspend (T) -> T?) {
    cache.edit {
      val oldCache = get(key) ?: defaultCache()
      val newCache = block(oldCache)
      if (newCache != null) {
        put(key, newCache)
      } else {
        remove(key)
      }
    }
  }
}