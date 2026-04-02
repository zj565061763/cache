package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

interface CacheKtx<T> {
  /** [key]对应的缓存 */
  fun flowOf(key: String): Flow<T?>

  /** 编辑缓存，[block]在[Dispatchers.IO]上面执行 */
  suspend fun <R> edit(block: suspend Cache<T>.() -> R): R
}

suspend fun <T> CacheKtx<T>.put(key: String, value: T?) = edit { put(key, value) }
suspend fun <T> CacheKtx<T>.get(key: String) = edit { get(key) }
suspend fun <T> CacheKtx<T>.remove(key: String) = edit { remove(key) }
suspend fun <T> CacheKtx<T>.keys() = edit { keys() }

internal class CacheKtxImpl<T>(
  val cache: CacheImpl<T>,
) : CacheKtx<T> {
  private val _callbacks = CacheCallbacks(cache)

  override fun flowOf(key: String): Flow<T?> {
    return callbackFlow {
      val callback = callbackForTargetKeyCacheChange(targetKey = key) { trySend(Unit) }
      _callbacks.addCallback(callback)
      awaitClose { _callbacks.removeCallback(callback) }
    }.conflate()
      .onStart { emit(Unit) }
      .map { cache.get(key) }
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
  }

  override suspend fun <R> edit(block: suspend Cache<T>.() -> R): R {
    return withContext(Dispatchers.IO) {
      libLock(cache) {
        try {
          runBlocking { block(cache) }
        } catch (e: InterruptedException) {
          throw CancellationException().initCause(e)
        }
      }
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
  }
}