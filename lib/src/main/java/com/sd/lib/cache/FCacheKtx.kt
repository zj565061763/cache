package com.sd.lib.cache

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface CacheKtx<T> {
    /** [key]对应的数据流 */
    fun flowOf(key: String): Flow<T?>

    /** 编辑缓存，[block]在[Dispatchers.IO]上面执行 */
    suspend fun <R> edit(block: suspend Cache<T>.() -> R): R
}

/**
 * 如果[key]对应的缓存存在，则调用[block]，并把[block]的返回值设置为缓存
 */
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
            val callback: suspend (T?) -> Unit = { send(it) }
            _callbacks.addCallback(key, callback)
            awaitClose { _callbacks.removeCallback(key, callback) }
        }.distinctUntilChanged()
    }

    override suspend fun <R> edit(block: suspend Cache<T>.() -> R): R {
        return withCacheContext {
            block(cache)
        }
    }
}

private class CacheCallbacks<T>(
    cache: Cache<T>,
) {
    private val cacheImpl = cache as CacheImpl<T>
    private val _callbacks = mutableMapOf<String, Set<suspend (T?) -> Unit>>()

    suspend fun addCallback(key: String, callback: suspend (T?) -> Unit) {
        synchronized(this@CacheCallbacks) {
            val set = _callbacks.getOrPut(key) { emptySet() }
            _callbacks[key] = set + callback
        }
        withCacheContext {
            cacheImpl.notifyChange(key)
        }
    }

    fun removeCallback(key: String, callback: suspend (T?) -> Unit) {
        synchronized(this@CacheCallbacks) {
            val set = _callbacks[key] ?: return
            val newSet = set - callback
            if (newSet.isEmpty()) {
                _callbacks.remove(key)
            } else {
                _callbacks[key] = newSet
            }
        }
    }

    private fun getCallbacks(key: String): Set<suspend (T?) -> Unit>? {
        return synchronized(this@CacheCallbacks) {
            _callbacks[key]
        }
    }

    init {
        check(cacheImpl.onChange == null)
        cacheImpl.onChange = { key, data ->
            val callbacks = getCallbacks(key)
            if (!callbacks.isNullOrEmpty()) {
                CacheScope.launch {
                    callbacks.forEach {
                        it.invoke(data)
                    }
                }
            }
        }
    }
}

private val CacheScope = MainScope() + CoroutineName("FCacheScope")

private suspend fun <R> withCacheContext(block: suspend () -> R): R {
    return withContext(Dispatchers.IO) {
        cacheLock {
            runBlocking {
                block()
            }
        }
    }
}