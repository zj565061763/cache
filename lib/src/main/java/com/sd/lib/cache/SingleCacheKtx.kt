package com.sd.lib.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

interface SingleCacheKtx<T> {
  /** 缓存 */
  fun flow(): Flow<T?>

  /** 编辑缓存，[block]在[Dispatchers.IO]上面执行 */
  suspend fun <R> edit(block: suspend SingleCache<T>.() -> R): R
}

/**
 * 如果缓存存在，则调用[block]，并把[block]的返回值设置为缓存。
 * 注意：如果[block]返回null，则本次调用不修改缓存。
 */
suspend fun <T> SingleCacheKtx<T>.update(block: suspend (T) -> T?) = edit {
  val data = get()
  if (data != null) put(block(data))
}

fun <T> CacheKtx<T>.asSingleCacheKtx(
  key: String = FCache.DEFAULT_SINGLE_CACHE_KEY,
): SingleCacheKtx<T> {
  require(this is CacheKtxImpl<T>)
  val singleCache = cache.asSingleCache(key)
  return object : SingleCacheKtx<T> {
    override fun flow(): Flow<T?> {
      return this@asSingleCacheKtx.flowOf(key)
    }

    override suspend fun <R> edit(block: suspend SingleCache<T>.() -> R): R {
      return this@asSingleCacheKtx.edit {
        block(singleCache)
      }
    }
  }
}