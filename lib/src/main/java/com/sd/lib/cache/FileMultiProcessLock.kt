package com.sd.lib.cache

import java.io.File
import java.io.RandomAccessFile

/** 多进程锁 */
internal fun <T> multiProcessLock(block: () -> T): T {
  return multiProcessLock.lock(block)
}

/** 多进程锁 */
private val multiProcessLock by lazy {
  FileMultiProcessLock(
    lockFile = CacheConfig.get().directory.resolve("cache.lock"),
    currentProcessLock = FCache,
  )
}

/**
 * 基于文件锁实现的多进程锁
 */
private class FileMultiProcessLock(
  private val lockFile: File,
  private val currentProcessLock: Any,
) {
  private var _raf: RandomAccessFile? = null

  fun <T> lock(block: () -> T): T {
    synchronized(currentProcessLock) {
      if (_raf == null) {
        val raf = RandomAccessFile(lockFile, "rw")
        return try {
          _raf = raf
          raf.channel.lock()
          block()
        } finally {
          runCatching { raf.close() }
          _raf = null
        }
      } else {
        return block()
      }
    }
  }

  init {
    lockFile.parentFile?.mkdirs()
  }
}