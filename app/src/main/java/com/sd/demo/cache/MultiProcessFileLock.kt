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
      val channel = getRF().channel
      runCatching {
        channel.tryLock()
      }.onSuccess { lock ->
        if (lock != null) {
          // 当前进程获得文件锁
          handleLockByCurrentProcess(lock, block)
        } else {
          // 其他进程获得文件锁
          handleLockByOtherProcess(block)
        }
      }.onFailure { e ->
        when (e) {
          is OverlappingFileLockException -> {
            // 当前进程已经获得文件锁
            block()
          }
          else -> {
            closeRFQuietly()
            throw e
          }
        }
      }
    }
  }

  /** 其他进程已经获得文件锁 */
  @Throws(Throwable::class)
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
          /** 理论上这里不会发生，因为[lock]已经检查过了 */
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
      runCatching { lock.release() }.onFailure { closeRFQuietly() }
    }
  }

  private fun getRF(): RandomAccessFile {
    return _rf ?: run {
      lockFile.fCreateFile()
      RandomAccessFile(lockFile, "rw").also { _rf = it }
    }
  }

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