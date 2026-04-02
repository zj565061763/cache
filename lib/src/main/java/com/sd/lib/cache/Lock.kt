package com.sd.lib.cache

import android.net.LocalServerSocket
import java.io.IOException

internal fun <T> lockCache(
  cache: CacheImpl<*>,
  block: () -> T,
): T {
  return when (cache.lockLevel) {
    CacheLockLevel.CurrentProcessCurrentCache -> synchronized(cache) { block() }
    CacheLockLevel.CurrentProcessCurrentGroup -> synchronized(cache.groupLock) { block() }
    CacheLockLevel.CurrentProcess -> synchronized(CurrentProcessLock) { block() }
    CacheLockLevel.MultiProcess -> MultiProcessLock.lock(
      id = "com.sd.lib.cache:${CacheConfig.get().context.packageName}",
      block = block,
    )
  }
}

/** 当前进程锁 */
private val CurrentProcessLock = Any()

/** 多进程锁 */
private object MultiProcessLock {
  private var _socket: LocalServerSocket? = null

  fun <T> lock(id: String, block: () -> T): T {
    synchronized(this@MultiProcessLock) {
      if (_socket == null) {
        val socket = realLock(id)
        return try {
          _socket = socket
          block()
        } finally {
          runCatching { socket.close() }
          _socket = null
        }
      } else {
        return block()
      }
    }
  }

  private fun realLock(name: String): LocalServerSocket {
    var retryCount = 0
    while (true) {
      try {
        return LocalServerSocket(name)
      } catch (e: IOException) {
        if (retryCount >= 1000) throw IOException("Get multi-process lock timeout", e)
        retryCount++
        try {
          Thread.sleep(10)
        } catch (ie: InterruptedException) {
          Thread.currentThread().interrupt()
          throw ie
        }
      }
    }
  }
}