package com.sd.lib.cache

import java.io.File
import java.io.RandomAccessFile

internal fun <T> libLock(block: () -> T): T {
  return fileProcessLock.lock(block)
}

private val fileProcessLock by lazy {
  FileProcessLock(
    lockFile = CacheConfig.get().directory.resolve("cache.lock"),
    currentProcessLock = FCache,
  )
}

private class FileProcessLock(
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