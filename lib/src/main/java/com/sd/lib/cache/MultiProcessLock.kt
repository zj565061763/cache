package com.sd.lib.cache

import kotlinx.coroutines.CancellationException
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.FileLockInterruptionException
import java.nio.channels.OverlappingFileLockException

/**
 * 多进程锁
 */
internal class MultiProcessLock(
  /** 要锁的文件 */
  private val lockFile: File,
) {
  private var _rf: RandomAccessFile? = null

  @Throws(Throwable::class)
  fun <T> lock(block: () -> T): T {
    val channel = getRF().channel
    val result = runCatching { channel.tryLock() }

    // 成功
    result.onSuccess { lock ->
      return if (lock != null) {
        // 当前进程获得文件锁
        handleLockByCurrentProcess(lock, block)
      } else {
        // 其他进程获得文件锁
        handleLockByOtherProcess(block)
      }
    }

    // 失败
    when (val exception = checkNotNull(result.exceptionOrNull())) {
      is OverlappingFileLockException -> {
        // 当前进程已经获得文件锁
        return block()
      }
      else -> {
        closeRFQuietly()
        throw exception
      }
    }
  }

  /** 其他进程已经获得文件锁，阻塞当前线程等待文件锁 */
  @Throws(Throwable::class)
  private fun <T> handleLockByOtherProcess(block: () -> T): T {
    val channel = getRF().channel
    val result = runCatching { channel.lock() }

    // 成功
    result.onSuccess { lock ->
      return handleLockByCurrentProcess(lock, block)
    }

    // 失败
    when (val exception = checkNotNull(result.exceptionOrNull())) {
      is OverlappingFileLockException -> {
        // 当前进程已经获得文件锁
        return block()
      }
      is FileLockInterruptionException -> {
        /** 当前线程在[FileChannel.lock]阻塞期间被取消，转为[CancellationException]异常 */
        throw CancellationException()
      }
      else -> {
        closeRFQuietly()
        throw exception
      }
    }
  }

  /** 当前进程获得文件锁 */
  private fun <T> handleLockByCurrentProcess(lock: FileLock, block: () -> T): T {
    return try {
      block()
    } finally {
      runCatching { lock.release() }.onFailure { closeRFQuietly() }
    }
  }

  @Synchronized
  private fun getRF(): RandomAccessFile {
    return _rf ?: run {
      lockFile.fCreateFile()
      RandomAccessFile(lockFile, "rw").also { _rf = it }
    }
  }

  @Synchronized
  private fun closeRFQuietly() {
    _rf?.also { file ->
      _rf = null
      runCatching { file.close() }
    }
  }
}

private fun File.fCreateFile(): Boolean {
  if (isFile) return true
  if (isDirectory) deleteRecursively()
  parentFile?.mkdirs()
  return createNewFile()
}