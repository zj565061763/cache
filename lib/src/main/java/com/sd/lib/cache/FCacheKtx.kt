package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

interface CacheKtx<T> {
  /** [key]对应的数据流 */
  fun flowOf(key: String): Flow<T?>

  /** 监听所有缓存 */
  fun flowOfAll(): Flow<List<T>>

  /** 监听所有key变化 */
  fun flowOfKeys(): Flow<List<String>>

  /** 编辑缓存，[block]在[Dispatchers.IO]上面执行 */
  suspend fun <R> edit(block: suspend Cache<T>.() -> R): R
}

/** 如果[key]对应的缓存存在，则调用[block]，并把[block]的返回值设置为缓存 */
suspend fun <T> CacheKtx<T>.update(key: String, block: suspend (T) -> T) {
  edit {
    val data = get(key)
    if (data != null) {
      put(key, block(data))
    }
  }
}

/** [Cache.put] */
suspend fun <T> CacheKtx<T>.put(key: String, value: T?): Boolean {
  return edit { put(key, value) }
}

/** [Cache.get] */
suspend fun <T> CacheKtx<T>.get(key: String): T? {
  return edit { get(key) }
}

/** [Cache.remove] */
suspend fun <T> CacheKtx<T>.remove(key: String) {
  return edit { remove(key) }
}

/** [Cache.contains] */
suspend fun <T> CacheKtx<T>.contains(key: String): Boolean {
  return edit { contains(key) }
}

/** [Cache.keys] */
suspend fun <T> CacheKtx<T>.keys(): List<String> {
  return edit { keys() }
}

object FCacheKtx {
  private val _caches = mutableMapOf<Class<*>, CacheKtx<*>>()

  fun <T> get(clazz: Class<T>): CacheKtx<T> {
    return synchronized(FCache) {
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
    val notifyFlow = MutableStateFlow(0L)
    return callbackFlow {
      val callback = targetCacheChangeCallback(
        targetKey = key,
        onChange = { notifyFlow.update { it + 1 } },
      )
      _callbacks.addCallback(callback)

      val notifyJob = launch {
        notifyFlow.collect {
          trySend(cache.get(key))
        }
      }

      awaitClose {
        _callbacks.removeCallback(callback)
        notifyJob.cancel()
      }
    }.conflate()
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
  }

  override fun flowOfAll(): Flow<List<T>> {
    return flowOfKeys()
      .map { keys -> keys.mapNotNull { key -> cache.get(key) } }
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
  }

  override fun flowOfKeys(): Flow<List<String>> {
    val notifyFlow = MutableStateFlow(0L)
    return callbackFlow {
      val callback = object : CacheStore.CacheChangeCallback {
        override fun onCreate(cacheKey: String) {
          notifyFlow.update { it + 1 }
        }

        override fun onModify(cacheKey: String) {
        }

        override fun onRemove(cacheKey: String) {
          notifyFlow.update { it + 1 }
        }
      }
      _callbacks.addCallback(callback)

      val notifyJob = launch {
        notifyFlow.collect {
          trySend(cache.keys())
        }
      }

      awaitClose {
        _callbacks.removeCallback(callback)
        notifyJob.cancel()
      }
    }.conflate()
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
  }

  override suspend fun <R> edit(block: suspend Cache<T>.() -> R): R {
    return withContext(Dispatchers.IO) {
      multiProcessLock {
        runBlocking {
          block(cache)
        }
      }
    }
  }
}

private class CacheCallbacks<T>(cache: Cache<T>) {
  private val _cache = cache as CacheImpl<T>
  private val _callbacks = Collections.newSetFromMap<CacheStore.CacheChangeCallback>(ConcurrentHashMap())

  fun addCallback(callback: CacheStore.CacheChangeCallback) {
    _callbacks.add(callback)
  }

  fun removeCallback(callback: CacheStore.CacheChangeCallback) {
    _callbacks.remove(callback)
  }

  init {
    check(_cache.cacheChangeCallback == null)
    _cache.cacheChangeCallback = object : CacheStore.CacheChangeCallback {
      override fun onCreate(cacheKey: String) {
        _callbacks.forEach { it.onCreate(cacheKey) }
      }

      override fun onModify(cacheKey: String) {
        _callbacks.forEach { it.onModify(cacheKey) }
      }

      override fun onRemove(cacheKey: String) {
        _callbacks.forEach { it.onRemove(cacheKey) }
      }
    }
  }
}

private fun targetCacheChangeCallback(
  targetKey: String,
  onChange: () -> Unit,
): CacheStore.CacheChangeCallback {
  return object : CacheStore.CacheChangeCallback {
    override fun onCreate(cacheKey: String) {
      if (cacheKey == targetKey) {
        onChange()
      }
    }

    override fun onModify(cacheKey: String) {
      if (cacheKey == targetKey) {
        onChange()
      }
    }

    override fun onRemove(cacheKey: String) {
      if (cacheKey == targetKey) {
        onChange()
      }
    }
  }
}