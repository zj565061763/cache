package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/** 协程版缓存，支持Flow监听 */
interface CacheKtx<T> {
  /** [key]对应的缓存流，缓存变化时发射最新值 */
  fun flowOf(key: String): Flow<T?>

  /**
   * 编辑缓存，[block]在[Dispatchers.IO]上面执行，并在执行期间持有当前缓存的锁。
   *
   * 在[block]中访问锁不同的其他缓存时，如果另一处以相反顺序嵌套访问，会造成死锁。
   * 需要跨缓存原子操作时，应让这些缓存共享同一把锁，
   * 即同组的[CacheLockLevel.CurrentProcessCurrentGroup]或[CacheLockLevel.CurrentProcess]。
   */
  suspend fun <R> edit(block: Cache<T>.() -> R): R
}

/** 设置缓存，在[CacheKtx.edit]中执行 */
suspend fun <T> CacheKtx<T>.put(key: String, value: T) = edit { put(key, value) }

/** 获取缓存，在[CacheKtx.edit]中执行 */
suspend fun <T> CacheKtx<T>.get(key: String) = edit { get(key) }

/** 删除缓存，在[CacheKtx.edit]中执行 */
suspend fun <T> CacheKtx<T>.remove(key: String) = edit { remove(key) }

/** 所有缓存key，在[CacheKtx.edit]中执行 */
suspend fun <T> CacheKtx<T>.keys() = edit { keys() }

internal class CacheKtxImpl<T>(
  val cache: CacheImpl<T>,
) : CacheKtx<T> {
  private val _callbacks = CacheCallbacks(cache)

  override fun flowOf(key: String): Flow<T?> {
    return cacheFlowOf(key)
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
      // 每个值都是重新读盘的完整状态，订阅者处理慢时只保留最新值
      .conflate()
  }

  /**
   * [key]对应的缓存，每次事件后重新读盘；
   * 读取失败时保留上一个值，首次读取失败则发射null，避免订阅者一直等待。
   */
  fun cacheFlowOf(key: String): Flow<T?> {
    return flow {
      var emitted = false
      eventFlowOf(key).collect {
        val result = cache.readCache(key)
        if (result.isSuccess || !emitted) {
          emit(result.getOrNull())
          emitted = true
        }
      }
    }
  }

  fun eventFlowOf(key: String): Flow<Unit> {
    return callbackFlow {
      val callback = callbackForTargetKeyCacheChange(targetKey = key) { trySend(Unit) }
      _callbacks.addCallback(callback)
      trySend(Unit)
      awaitClose { _callbacks.removeCallback(callback) }
    }.conflate()
  }

  override suspend fun <R> edit(block: Cache<T>.() -> R): R {
    return withContext(Dispatchers.IO) {
      cache.lockCache { block(cache) }
    }
  }
}

private class CacheCallbacks<T>(cache: CacheImpl<T>) {
  private val _callbacks = Collections.newSetFromMap<CacheStore.CacheChangeCallback>(ConcurrentHashMap())

  fun addCallback(callback: CacheStore.CacheChangeCallback) {
    _callbacks.add(callback)
  }

  fun removeCallback(callback: CacheStore.CacheChangeCallback) {
    _callbacks.remove(callback)
  }

  init {
    check(cache.cacheChangeCallback == null)
    cache.cacheChangeCallback = object : CacheStore.CacheChangeCallback {
      override fun onRemove(key: String) {
        _callbacks.forEach { it.onRemove(key) }
      }

      override fun onModify(key: String) {
        _callbacks.forEach { it.onModify(key) }
      }

      override fun onCleared() {
        _callbacks.forEach { it.onCleared() }
      }
    }
  }
}

private fun callbackForTargetKeyCacheChange(
  targetKey: String,
  onChange: () -> Unit,
): CacheStore.CacheChangeCallback {
  return object : CacheStore.CacheChangeCallback {
    override fun onRemove(key: String) {
      if (key == targetKey) onChange()
    }

    override fun onModify(key: String) {
      if (key == targetKey) onChange()
    }

    override fun onCleared() {
      onChange()
    }
  }
}
