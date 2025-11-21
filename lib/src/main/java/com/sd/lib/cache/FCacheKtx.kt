package com.sd.lib.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface CacheKtx<T> {
  /** [key]对应的数据流 */
  fun flowOf(key: String): Flow<T?>

  /** 编辑缓存，[block]在[Dispatchers.IO]上面执行 */
  suspend fun <R> edit(block: suspend Cache<T>.() -> R): R
}

/** 如果[key]对应的缓存存在，则调用[block]，并把[block]的返回值设置为缓存 */
suspend fun <T> CacheKtx<T>.update(key: String, block: suspend (T) -> T) {
  edit {
    val data = get(key)
    if (data != null) {
      val newData = block(data)
      if (newData != data) {
        put(key, newData)
      }
    }
  }
}

object FCacheKtx {
  private val _caches = mutableMapOf<Class<*>, CacheKtx<*>>()

  fun <T> get(clazz: Class<T>): CacheKtx<T> {
    return cacheLock {
      val cache = _caches.getOrPut(clazz) { CacheKtxImpl(FCache.get(clazz)) }
      @Suppress("UNCHECKED_CAST")
      cache as CacheKtx<T>
    }
  }
}

private class CacheKtxImpl<T>(
  private val cache: Cache<T>,
) : CacheKtx<T> {
  private val _callbacks = CacheCallbacks(cache)

  override fun flowOf(key: String): Flow<T?> {
    return callbackFlow {
      val callback: (T?) -> Unit = { trySend(it) }
      _callbacks.addCallback(key, callback)

      val first = cache.get(key)
      trySend(first)

      awaitClose { _callbacks.removeCallback(key, callback) }
    }.conflate()
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
  }

  override suspend fun <R> edit(block: suspend Cache<T>.() -> R): R {
    return withContext(Dispatchers.IO) {
      cacheLock {
        runBlocking {
          block(cache)
        }
      }
    }
  }
}

private class CacheCallbacks<T>(cache: Cache<T>) {
  private val _cache = cache as CacheImpl<T>
  private val _callbacks = mutableMapOf<String, Set<(T?) -> Unit>>()

  fun addCallback(key: String, callback: (T?) -> Unit) {
    synchronized(_callbacks) {
      val set = _callbacks.getOrPut(key) { emptySet() }
      _callbacks[key] = set + callback
    }
  }

  fun removeCallback(key: String, callback: (T?) -> Unit) {
    synchronized(_callbacks) {
      val set = _callbacks[key] ?: return@synchronized
      val newSet = set - callback
      if (newSet.isEmpty()) {
        _callbacks.remove(key)
      } else {
        _callbacks[key] = newSet
      }
    }
  }

  private fun getCallbacks(key: String): Set<(T?) -> Unit>? {
    synchronized(_callbacks) {
      return _callbacks[key]
    }
  }

  init {
    check(_cache.onChange == null)
    _cache.onChange = { key, data ->
      getCallbacks(key)?.forEach { it.invoke(data) }
    }
  }
}