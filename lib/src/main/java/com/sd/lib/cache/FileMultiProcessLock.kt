package com.sd.lib.cache

import java.io.File
import java.io.RandomAccessFile

/**
 * 基于文件锁实现的多进程锁
 */
internal class FileMultiProcessLock(
  private val lockFile: File,
  private val currentProcessLock: Any,
) {
  private var _raf: RandomAccessFile? = null

  fun <T> lock(block: () -> T): T {
    synchronized(currentProcessLock) {
      if (_raf == null) {
        lockFile.parentFile?.mkdirs()
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
}