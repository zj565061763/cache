package com.sd.lib.cache

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch

interface SingleCacheKtx<T> {
  /** 缓存 */
  fun flow(): Flow<T>

  /**
   * 更新缓存，[block]在[Dispatchers.IO]上面执行，如果[block]返回null则删除缓存
   * @return true-更新成功；false-更新失败
   */
  suspend fun update(block: (T) -> T?): Boolean

  companion object {
    private val _caches = mutableMapOf<Class<*>, SingleCacheKtx<*>>()

    fun <T> get(
      clazz: Class<T>,
      /** 是否启用内存缓存，启用后[flow]方法返回的是热流，并缓存最近的一个值在内存中 */
      memoryCache: Boolean = false,
      /** 获取默认缓存，调用此方法时同步执行 */
      getDefault: () -> T,
    ): SingleCacheKtx<T> {
      return if (memoryCache) {
        synchronized(_caches) {
          val cache = _caches.getOrPut(clazz) {
            MemorySingleCacheKtx(
              cache = FCache.getKtx(clazz),
              defaultCache = getDefault(),
            )
          }
          @Suppress("UNCHECKED_CAST")
          cache as SingleCacheKtx<T>
        }
      } else {
        DiskSingleCacheKtx(
          cache = FCache.getKtx(clazz),
          defaultCache = getDefault(),
        )
      }
    }
  }
}

/** 获取当前缓存值 */
suspend fun <T> SingleCacheKtx<T>.get(): T = flow().first()

inline fun <reified T> singleCacheKtx(
  /** 是否启用内存缓存，启用后[SingleCacheKtx.flow]方法返回的是热流，并缓存最近的一个值在内存中 */
  memoryCache: Boolean = false,
  /** 获取默认缓存，调用此方法时同步执行 */
  noinline getDefault: () -> T,
): SingleCacheKtx<T> {
  return SingleCacheKtx.get(
    clazz = T::class.java,
    memoryCache = memoryCache,
    getDefault = getDefault,
  )
}

private abstract class BaseSingleCacheKtx<T>(
  protected val cache: CacheKtx<T>,
  protected val defaultCache: T,
  protected val key: String = "com.sd.lib.cache.key.singlecache",
) : SingleCacheKtx<T> {

  override suspend fun update(block: (T) -> T?): Boolean {
    return cache.edit {
      val oldCache = get(key) ?: defaultCache
      val newCache = block(oldCache)
      val result = if (newCache != null) {
        put(key, newCache)
      } else {
        remove(key)
      }
      if (result) {
        onUpdateResult(newCache)
      }
      result
    }
  }

  /** [update] 写入成功后回调，[newCache] 为写入的值，null 表示已删除 */
  protected open fun onUpdateResult(newCache: T?) {}
}

/**
 * 不启用内存缓存：[flow] 为冷流，每次订阅都从磁盘读取。
 */
private class DiskSingleCacheKtx<T>(
  cache: CacheKtx<T>,
  defaultCache: T,
) : BaseSingleCacheKtx<T>(cache, defaultCache) {

  private val _flow = cache.eventFlowOf(key)
    .map { cache.get(key) ?: defaultCache }
    .distinctUntilChanged()
    .flowOn(Dispatchers.IO)

  override fun flow(): Flow<T> = _flow
}

/**
 * 启用内存缓存：[flow] 为热流（replay=1），值变化时同步更新内存。
 * 订阅建立后若热流尚未初始化，自动读一次磁盘填充初始值。
 */
@OptIn(DelicateCoroutinesApi::class)
private class MemorySingleCacheKtx<T>(
  cache: CacheKtx<T>,
  defaultCache: T,
) : BaseSingleCacheKtx<T>(cache, defaultCache) {

  private val _hotFlow = MutableSharedFlow<T?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  private val _flow = _hotFlow
    .onSubscription { if (_hotFlow.replayCache.isEmpty()) emit(cache.get(key)) }
    .map { it ?: defaultCache }
    .distinctUntilChanged()
    .flowOn(Dispatchers.IO)

  override fun flow(): Flow<T> = _flow

  override fun onUpdateResult(newCache: T?) {
    _hotFlow.tryEmit(newCache)
  }

  init {
    GlobalScope.launch {
      cache.eventFlowOf(key).collect {
        cache.edit { _hotFlow.tryEmit(get(key)) }
      }
    }
  }
}
