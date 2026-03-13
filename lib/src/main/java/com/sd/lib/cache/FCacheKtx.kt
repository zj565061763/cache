package com.sd.lib.cache

import com.sd.lib.cache.store.CacheStore
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

  /** 监听所有key变化 */
  fun flowOfKeys(): Flow<List<String>>

  /** 编辑缓存，[block]在[Dispatchers.IO]上面执行 */
  suspend fun <R> edit(block: suspend Cache<T>.() -> R): R
}

/**
 * 如果[key]对应的缓存存在，则调用[block]，并把[block]的返回值设置为缓存。
 * 注意：如果[block]返回null，则本次调用不修改缓存。
 */
suspend fun <T> CacheKtx<T>.update(key: String, block: suspend (T) -> T?) = edit {
  val data = get(key)
  if (data != null) {
    put(key, block(data))
  }
}

/** [Cache.put] */
suspend fun <T> CacheKtx<T>.put(key: String, value: T?): Boolean = edit { put(key, value) }

/** [Cache.get] */
suspend fun <T> CacheKtx<T>.get(key: String): T? = edit { get(key) }

/** [Cache.remove] */
suspend fun <T> CacheKtx<T>.remove(key: String) = edit { remove(key) }

/** [Cache.contains] */
suspend fun <T> CacheKtx<T>.contains(key: String): Boolean = edit { contains(key) }

/** [Cache.keys] */
suspend fun <T> CacheKtx<T>.keys(): List<String> = edit { keys() }

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

  override fun flowOfKeys(): Flow<List<String>> {
    return callbackFlow {
      val callback = callbackForTargetKeyCacheChange(targetKey = null) { trySend(Unit) }
      _callbacks.addCallback(callback)
      awaitClose { _callbacks.removeCallback(callback) }
    }.conflate()
      .onStart { emit(Unit) }
      .map { cache.keys() }
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
  }

  override suspend fun <R> edit(block: suspend Cache<T>.() -> R): R {
    return withContext(Dispatchers.IO) {
      libLock {
        runBlocking { block(cache) }
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
  targetKey: String?,
  onChange: () -> Unit,
): CacheStore.CacheChangeCallback {
  return object : CacheStore.CacheChangeCallback {
    override fun onRemove(key: String) {
      if (targetKey == null || targetKey == key) {
        onChange()
      }
    }

    override fun onModify(key: String) {
      if (targetKey == null || targetKey == key) {
        onChange()
      }
    }
  }
}