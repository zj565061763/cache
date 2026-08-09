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
            SingleCacheKtxImpl(
              cache = FCache.getKtx(clazz),
              memoryCache = true,
              defaultCache = getDefault(),
            )
          }
          @Suppress("UNCHECKED_CAST")
          cache as SingleCacheKtx<T>
        }
      } else {
        SingleCacheKtxImpl(
          cache = FCache.getKtx(clazz),
          memoryCache = false,
          defaultCache = getDefault(),
        )
      }
    }
  }
}

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

@OptIn(DelicateCoroutinesApi::class)
private class SingleCacheKtxImpl<T>(
  private val cache: CacheKtx<T>,
  private val key: String = "com.sd.lib.cache.key.singlecache",
  memoryCache: Boolean,
  private val defaultCache: T,
) : SingleCacheKtx<T> {
  private val _hotFlow: MutableSharedFlow<T?>? = if (memoryCache) {
    MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  } else {
    null
  }

  private val _flow = (_hotFlow ?: (cache.eventFlowOf(key).map { cache.get(key) }))
    .map { it ?: defaultCache }
    .distinctUntilChanged()
    .flowOn(Dispatchers.IO)

  override fun flow(): Flow<T> = _flow

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
