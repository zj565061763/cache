package com.sd.lib.cache

import android.net.LocalServerSocket
import java.io.IOException

internal fun <T> libLock(
  multiProcess: Boolean,
  block: () -> T,
): T {
  return if (multiProcess) {
    socketProcessLock.lock(block)
  } else {
    synchronized(FCache) { block() }
  }
}

private val socketProcessLock by lazy {
  SocketProcessLock(
    name = "com.sd.lib.cache:${CacheConfig.get().context.packageName}",
    currentProcessLock = FCache,
  )
}

private class SocketProcessLock(
  private val name: String,
  private val currentProcessLock: Any,
) {
  private var _socket: LocalServerSocket? = null

  fun <T> lock(block: () -> T): T {
    synchronized(currentProcessLock) {
      if (_socket == null) {
        val socket = realLock()
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

  private fun realLock(): LocalServerSocket {
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