package com.sd.lib.cache

import android.net.LocalServerSocket
import java.io.IOException

/**
 * 多进程锁
 */
internal class MultiProcessLock(
  /** 当前进程锁 */
  private val currentProcessLock: Any,
) {
  private var _socket: LocalServerSocket? = null

  fun <T> lock(block: () -> T): T {
    synchronized(currentProcessLock) {
      try {
        lock()
        return block()
      } finally {
        unlock()
      }
    }
  }

  private fun lock() {
    while (true) {
      try {
        if (_socket == null) {
          _socket = LocalServerSocket("com.sd.lib.cache")
        }
        return
      } catch (e: IOException) {
        runCatching { Thread.sleep(10) }
      }
    }
  }

  private fun unlock() {
    _socket?.also { server ->
      _socket = null
      runCatching { server.close() }
    }
  }
}