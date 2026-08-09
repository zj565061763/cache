package com.sd.lib.cache

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface SingleCacheKtx<T> {
  /** 缓存 */
  fun flow(): Flow<T>

  /**
   * 更新缓存，[block]在[Dispatchers.IO]上面执行，如果[block]返回null则删除缓存
   * @return true-更新成功；false-更新失败
   */
  suspend fun update(block: (T) -> T?): Boolean
}

/**
 * 注意：如果启用了内存缓存（[memoryCache]为true），则App中同类型的缓存[T]，应该使用同一个[SingleCacheKtx]对象。
 * 否者可能造成[SingleCacheKtx.flow]延迟，例如：
 * A对象调用[SingleCacheKtx.update]后，B对象立即从[SingleCacheKtx.flow]获取的值可能还是旧的。
 *
 * 另外启用内存缓存后，内部会在[GlobalScope]上常驻一个协程收集缓存变化，它不会被取消，
 * 所以不要为同一份缓存重复创建[SingleCacheKtx]对象，例如放在Activity的字段上。
 */
fun <T> CacheKtx<T>.asSingleCacheKtx(
  /** 缓存key */
  key: String = DEFAULT_SINGLE_CACHE_KEY,
  /** 是否启用内存缓存，启用后[SingleCacheKtx.flow]方法返回的是热流，并缓存最近的一个值在内存中 */
  memoryCache: Boolean = false,
  /**
   * 获取默认缓存，在[Dispatchers.IO]上面执行。
   * 每个收集者会各自调用它，所以应该保持轻量，并且多次调用要返回相等的值，
   * 否则不同的收集者可能拿到不同的默认缓存。
   */
  getDefault: () -> T,
): SingleCacheKtx<T> {
  return SingleCacheKtxImpl(
    cache = this,
    key = key,
    memoryCache = memoryCache,
    getDefault = getDefault,
  )
}

@OptIn(DelicateCoroutinesApi::class)
private class SingleCacheKtxImpl<T>(
  private val cache: CacheKtx<T>,
  private val key: String,
  memoryCache: Boolean,
  private val getDefault: () -> T,
) : SingleCacheKtx<T> {
  private val _hotFlow: MutableSharedFlow<T?>? = if (memoryCache) {
    MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  } else {
    null
  }

  private val _flow = (_hotFlow ?: (cache.eventFlowOf(key).map { cache.get(key) }))
    .map { it ?: getDefault() }
    .distinctUntilChanged()
    .flowOn(Dispatchers.IO)

  override fun flow(): Flow<T> = _flow

  override suspend fun update(block: (T) -> T?): Boolean {
    return cache.edit {
      val oldCache = get(key) ?: getDefault()
      val newCache = block(oldCache)
      val result = if (newCache != null) {
        put(key, newCache)
      } else {
        remove(key)
      }
      if (result) {
        _hotFlow?.tryEmit(newCache)
      }
      result
    }
  }

  init {
    val hotFlow = _hotFlow
    if (hotFlow != null) {
      GlobalScope.launch {
        cache.eventFlowOf(key).collect {
          cache.edit { hotFlow.tryEmit(get(key)) }
        }
      }
    }
  }
}

/** 默认的单缓存key */
private const val DEFAULT_SINGLE_CACHE_KEY = "com.sd.lib.cache.key.singlecache"