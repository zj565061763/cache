package com.sd.demo.cache

import kotlinx.coroutines.CancellationException
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.FileLockInterruptionException
import java.nio.channels.OverlappingFileLockException

/**
 * 多进程文件锁
 */
internal class MultiProcessFileLock(
  /** 要锁的文件 */
  private val lockFile: File,
  /** 当前进程锁 */
  private val currentProcessLock: Any,
) {
  private var _rf: RandomAccessFile? = null

  @Throws(Throwable::class)
  fun lock(block: () -> Unit) {
    synchronized(currentProcessLock) {
      when (val result = tryLockFile()) {
        is LockFileResult.Lock -> {
          val lock = result.lock
          if (lock != null) {
            // 当前进程获得文件锁
            handleLockByCurrentProcess(lock, block)
          } else {
            // 其他进程获得文件锁
            handleLockByOtherProcess(block)
          }
        }
        is LockFileResult.Overlapping -> {
          // 当前进程已经获得文件锁
          block()
        }
        is LockFileResult.Other -> {
          throw result.e
        }
      }
    }
  }

  /** 其他进程已经获得文件锁 */
  private fun handleLockByOtherProcess(block: () -> Unit) {
    val channel = getRF().channel
    runCatching {
      channel.lock()
    }.onSuccess { lock ->
      handleLockByCurrentProcess(lock, block)
    }.onFailure { e ->
      when (e) {
        is FileLockInterruptionException -> {
          /** 当前线程在[FileChannel.lock]阻塞期间被取消，转为[CancellationException]异常 */
          throw CancellationException()
        }
        is OverlappingFileLockException -> {
          /** 理论上这里不会发生，因为[lock]被[currentProcessLock]锁住了，且[tryLockFile]已经检查过了 */
          throw e
        }
        else -> {
          closeRFQuietly()
          throw e
        }
      }
    }
  }

  /** 当前进程获得文件锁 */
  private fun handleLockByCurrentProcess(
    lock: FileLock,
    block: () -> Unit,
  ) {
    try {
      block()
    } finally {
      releaseLock(lock)
    }
  }

  /** 释放文件锁 */
  private fun releaseLock(lock: FileLock) {
    runCatching {
      lock.release()
    }.onFailure {
      closeRFQuietly()
    }
  }

  /** 获得文件锁 */
  private fun tryLockFile(): LockFileResult {
    val channel = getRF().channel
    return runCatching {
      val lock = channel.tryLock()
      LockFileResult.Lock(lock)
    }.getOrElse { e ->
      when (e) {
        is OverlappingFileLockException -> {
          LockFileResult.Overlapping(e)
        }
        else -> {
          closeRFQuietly()
          LockFileResult.Other(e)
        }
      }
    }
  }

  private fun getRF(): RandomAccessFile {
    return _rf ?: run {
      lockFile.fCreateFile()
      RandomAccessFile(lockFile, "rw")
    }.also { _rf = it }
  }

  private fun closeRFQuietly() {
    _rf?.also { file ->
      _rf = null
      runCatching { file.close() }
    }
  }
}

sealed interface LockFileResult {
  data class Lock(val lock: FileLock?) : LockFileResult
  data class Overlapping(val e: OverlappingFileLockException) : LockFileResult
  data class Other(val e: Throwable) : LockFileResult
}

private fun File.fCreateFile(): Boolean {
  if (isFile) return true
  if (isDirectory) deleteRecursively()
  parentFile?.mkdirs()
  return createNewFile()
}
