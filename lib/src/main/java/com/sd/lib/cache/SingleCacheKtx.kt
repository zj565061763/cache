package com.sd.lib.cache

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface SingleCacheKtx<T> {
  /** 缓存 */
  fun flow(): Flow<T>

  /**
   * 更新缓存，[block]在[Dispatchers.IO]上面执行，如果[block]返回null则删除缓存
   * @return true-更新成功；false-更新失败
   */
  suspend fun update(block: suspend (T) -> T?): Boolean
}

/**
 * 注意：如果[coroutineScope]不为null，说明启用了内存缓存，则App中同类型的缓存[T]，应该使用同一个[SingleCacheKtx]对象。
 * 否者可能造成[SingleCacheKtx.flow]延迟，例如：
 * A对象调用[SingleCacheKtx.update]后，B对象立即从[SingleCacheKtx.flow]获取的值可能还是旧的。
 */
fun <T> CacheKtx<T>.asSingleCacheKtx(
  /** 缓存key */
  key: String = DEFAULT_SINGLE_CACHE_KEY,
  /** 如果不为null，则[SingleCacheKtx.flow]方法返回的是热流，并缓存最近的一个值在内存中 */
  coroutineScope: CoroutineScope? = null,
  /** 获取默认缓存，在[Dispatchers.IO]上面执行 */
  getDefault: () -> T,
): SingleCacheKtx<T> {
  return SingleCacheKtxImpl(
    cache = this,
    key = key,
    coroutineScope = coroutineScope,
    getDefault = getDefault,
  )
}

private class SingleCacheKtxImpl<T>(
  private val cache: CacheKtx<T>,
  private val key: String,
  private val coroutineScope: CoroutineScope?,
  private val getDefault: () -> T,
) : SingleCacheKtx<T> {

  private val _flow = cache.flowOf(key)
    .map { it ?: getDefault() }
    .distinctUntilChanged()
    .flowOn(Dispatchers.IO)

  private val _sharedFlow: MutableSharedFlow<T>? = if (coroutineScope != null) {
    MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  } else {
    null
  }

  private val _hotFlow: Flow<T>? = _sharedFlow?.distinctUntilChanged()

  override fun flow(): Flow<T> {
    return if (coroutineScope?.isActive == true) {
      checkNotNull(_hotFlow)
    } else {
      _flow
    }
  }

  override suspend fun update(block: suspend (T) -> T?): Boolean {
    return cache.edit {
      val oldCache = get(key) ?: getDefault()
      val newCache = block(oldCache)
      val result = if (newCache != null) {
        put(key, newCache)
      } else {
        remove(key)
      }
      if (result) {
        _sharedFlow?.tryEmit(newCache ?: getDefault())
      }
      result
    }
  }

  init {
    coroutineScope?.launch {
      val sharedFlow = checkNotNull(_sharedFlow)
      try {
        sharedFlow.emitAll(_flow)
      } catch (e: CancellationException) {
        @OptIn(ExperimentalCoroutinesApi::class)
        sharedFlow.resetReplayCache()
        throw e
      }
    }
  }
}

/** 默认的单缓存key */
private const val DEFAULT_SINGLE_CACHE_KEY = "com.sd.lib.cache.key.singlecache"