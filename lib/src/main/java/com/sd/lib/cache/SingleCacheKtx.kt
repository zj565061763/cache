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

/** 单值缓存，每个实体类型只保存一个值 */
interface SingleCacheKtx<T> {
  /** 缓存流 */
  fun flow(): Flow<T>

  /**
   * 更新缓存，[block]在[Dispatchers.IO]上面执行，如果[block]返回null则删除缓存
   * @return true-更新成功；false-更新失败
   */
  suspend fun update(block: (T) -> T?): Boolean

  companion object {
    private val _caches = mutableMapOf<Class<*>, SingleCacheKtx<*>>()

    /**
     * 获取[clazz]对应的[SingleCacheKtx]
     *
     * [memoryCache]为true时进程内共享同一实例，[getDefault]只在首次创建时调用一次；
     * 为false时每次返回新实例，并调用一次[getDefault]。
     */
    fun <T> get(
      clazz: Class<T>,
      /** 是否启用内存缓存，启用后[flow]方法返回的是热流，并缓存最近的一个值在内存中 */
      memoryCache: Boolean = false,
      /** 默认缓存，创建实例时同步调用 */
      getDefault: () -> T,
    ): SingleCacheKtx<T> {
      return if (memoryCache) {
        synchronized(_caches) {
          val cache = _caches.getOrPut(clazz) {
            MemorySingleCacheKtx(
              cache = FCache.getKtx(clazz) as CacheKtxImpl<T>,
              defaultCache = getDefault(),
            )
          }
          @Suppress("UNCHECKED_CAST")
          cache as SingleCacheKtx<T>
        }
      } else {
        DiskSingleCacheKtx(
          cache = FCache.getKtx(clazz) as CacheKtxImpl<T>,
          defaultCache = getDefault(),
        )
      }
    }
  }
}

/** 获取当前缓存值 */
suspend fun <T> SingleCacheKtx<T>.get(): T = flow().first()

/** 参考[SingleCacheKtx.Companion.get] */
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
  protected val cache: CacheKtxImpl<T>,
  private val defaultCache: T,
  protected val key: String = "com.sd.lib.cache.key.singlecache",
) : SingleCacheKtx<T> {

  private val _flow by lazy {
    getFlow()
      .map { it ?: defaultCache }
      .distinctUntilChanged()
  }

  final override fun flow(): Flow<T> = _flow

  final override suspend fun update(block: (T) -> T?): Boolean {
    return cache.edit {
      // 仓库读取失败时无法确定旧值，放弃更新以免覆盖已有缓存；解码失败视为无缓存，允许用默认值覆盖
      val oldCache = cache.cache.readCache(key).getOrElse { return@edit false } ?: defaultCache
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

  /** [update]写入成功后回调，[newCache]为写入的值，null表示已删除 */
  protected open fun onUpdateResult(newCache: T?) = Unit
}

/**
 * 磁盘缓存：[flow]为冷流，每次订阅都从磁盘读取
 */
private class DiskSingleCacheKtx<T>(
  cache: CacheKtxImpl<T>,
  defaultCache: T,
) : BaseSingleCacheKtx<T>(cache, defaultCache) {
  override fun getFlow(): Flow<T?> {
    return cache.cacheFlowOf(key)
      .flowOn(Dispatchers.IO)
  }
}

/**
 * 磁盘和内存缓存：[flow]为热流，值变化时同步更新内存
 */
@OptIn(DelicateCoroutinesApi::class)
private class MemorySingleCacheKtx<T>(
  cache: CacheKtxImpl<T>,
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
    completeInitialized()
  }

  private fun completeInitialized() {
    if (_initialized.isCompleted) return
    _initialized.complete(Unit)
  }

  init {
    GlobalScope.launch {
      cache.eventFlowOf(key).collect {
        cache.edit {
          val result = cache.cache.readCache(key)
          // 读取失败时保留内存中的值；尚未初始化时仍发射null，避免订阅者一直等待
          if (result.isSuccess || !_initialized.isCompleted) {
            _hotFlow.tryEmit(result.getOrNull())
            completeInitialized()
          }
        }
      }
    }
  }
}
