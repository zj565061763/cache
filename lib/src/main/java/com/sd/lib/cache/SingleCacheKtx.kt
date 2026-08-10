package com.sd.lib.cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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

  companion object {
    private val _caches = mutableMapOf<Class<*>, SingleCacheKtx<*>>()

    /**
     * 获取[clazz]对应的[SingleCacheKtx]，
     * 如果[memoryCache]为true，则进程共享同一个[SingleCacheKtx]实例，并且[getDefault]只在实例首次被创建时，同步调用一次,
     * 如果[memoryCache]为false，则每次调用都返回一个新的[SingleCacheKtx]实例，且同步调用一次[getDefault]
     */
    fun <T> get(
      clazz: Class<T>,
      /** 是否启用内存缓存，启用后[flow]方法返回的是热流，并缓存最近的一个值在内存中 */
      memoryCache: Boolean = false,
      /** 获取默认缓存，调用此方法时，按需同步执行 */
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

/** 参考[SingleCacheKtx.get] */
inline fun <reified T> singleCacheKtx(
  memoryCache: Boolean = false,
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
  private val defaultCache: T,
  protected val key: String = "com.sd.lib.cache.key.singlecache",
) : SingleCacheKtx<T> {

  private val _flow by lazy {
    getFlow()
      .map { it ?: defaultCache }
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
  }

  final override fun flow(): Flow<T> = _flow

  final override suspend fun update(block: (T) -> T?): Boolean {
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

  protected abstract fun getFlow(): Flow<T?>

  /** [update] 写入成功后回调，[newCache] 为写入的值，null 表示已删除 */
  protected open fun onUpdateResult(newCache: T?) = Unit
}

/**
 * 磁盘缓存：[flow] 为冷流，每次订阅都从磁盘读取
 */
private class DiskSingleCacheKtx<T>(
  cache: CacheKtx<T>,
  defaultCache: T,
) : BaseSingleCacheKtx<T>(cache, defaultCache) {
  override fun getFlow(): Flow<T?> {
    return cache.eventFlowOf(key).map { cache.get(key) }
  }
}

/**
 * 磁盘和内存缓存：[flow] 为热流，值变化时同步更新内存
 */
@OptIn(DelicateCoroutinesApi::class)
private class MemorySingleCacheKtx<T>(
  cache: CacheKtx<T>,
  defaultCache: T,
) : BaseSingleCacheKtx<T>(cache, defaultCache) {
  private val _initialized = CompletableDeferred<Unit>()
  private val _hotFlow = MutableSharedFlow<T?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  override fun getFlow(): Flow<T?> {
    return flow {
      _initialized.await()
      emitAll(_hotFlow)
    }
  }

  override fun onUpdateResult(newCache: T?) {
    _hotFlow.tryEmit(newCache)
  }

  init {
    GlobalScope.launch {
      cache.eventFlowOf(key).collect {
        cache.edit { _hotFlow.tryEmit(get(key)) }
        if (!_initialized.isCompleted) _initialized.complete(Unit)
      }
    }
  }
}
