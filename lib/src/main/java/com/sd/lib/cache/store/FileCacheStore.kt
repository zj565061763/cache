package com.sd.lib.cache.store

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.FileObserver
import android.os.Process
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.Base64
import com.sd.lib.cache.libException
import com.sd.lib.cache.md5
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

internal class FileCacheStore : CacheStore {
  private lateinit var _directory: File
  private lateinit var _tempFilePrefix: String

  /** 监听是否有效 */
  @Volatile
  private var _watchValid = false

  @Volatile
  private var _cacheChangeCallback: CacheStore.CacheChangeCallback? = null

  override fun init(context: Context, directory: File) {
    if (::_directory.isInitialized) return
    _directory = directory
    _tempFilePrefix = tempFilePrefix(context)
    checkDirectoryExist()
    deleteTempFile()
  }

  override fun putCache(key: String, value: ByteArray) {
    checkWatchValid()
    val file = fileOf(key)

    fun writeWithTempFile() {
      val tempFile = File.createTempFile(_tempFilePrefix, TEMP_SUFFIX_WITH_DOT, _directory)
      try {
        FileOutputStream(tempFile).use { output ->
          output.write(value)
          // 重命名前把数据刷到磁盘，避免断电后留下空文件或写了一半的文件
          output.fd.sync()
        }
        if (!tempFile.renameTo(file)) {
          throw IOException("CacheStore.putCache rename failed from $tempFile to $file")
        }
      } finally {
        tempFile.delete()
      }
    }

    try {
      writeWithTempFile()
    } catch (e: IOException) {
      if (!_directory.isDirectory) {
        checkDirectoryExist()
        writeWithTempFile()
      } else {
        throw e
      }
    }
  }

  override fun getCache(key: String): ByteArray? {
    checkWatchValid()
    val file = fileOf(key)
    return try {
      file.readBytes()
    } catch (e: FileNotFoundException) {
      if (e.isPathMissing(file)) {
        checkDirectoryExist()
        null
      } else {
        throw e
      }
    }
  }

  override fun removeCache(key: String): Boolean {
    checkWatchValid()
    val file = fileOf(key)
    return try {
      Os.remove(file.absolutePath)
      true
    } catch (e: ErrnoException) {
      if (e.isPathMissing()) {
        checkDirectoryExist()
        true
      } else {
        throw IOException("CacheStore.removeCache failure:$file", e)
      }
    }
  }

  override fun keys(): List<String> {
    checkWatchValid()
    val listFile = _directory.listFiles { file -> file.name.endsWith(CACHE_SUFFIX_WITH_DOT) }
    if (listFile == null) {
      if (_directory.isDirectory) throw IOException("CacheStore.keys list files failure:$_directory")
      checkDirectoryExist()
      return emptyList()
    }
    if (listFile.isEmpty()) return emptyList()
    return listFile.mapNotNull { file ->
      val stat = try {
        Os.lstat(file.absolutePath)
      } catch (e: ErrnoException) {
        if (e.isPathMissing()) {
          return@mapNotNull null
        } else {
          throw IOException("CacheStore.keys stat failure:$file", e)
        }
      }

      if (!OsConstants.S_ISREG(stat.st_mode) || stat.st_size <= 0L) {
        return@mapNotNull null
      }

      val filename = file.name.removeSuffix(CACHE_SUFFIX_WITH_DOT)
      filenameToKey(filename)
    }
  }

  override fun setCacheChangeCallback(callback: CacheStore.CacheChangeCallback) {
    _cacheChangeCallback = callback
  }

  private val _fileObserver: FileObserver by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      object : FileObserver(_directory, FILE_OBSERVER_MASK) {
        override fun onEvent(event: Int, path: String?) = handleFileEvent(event, path)
      }
    } else {
      @Suppress("DEPRECATION")
      object : FileObserver(_directory.absolutePath, FILE_OBSERVER_MASK) {
        override fun onEvent(event: Int, path: String?) = handleFileEvent(event, path)
      }
    }
  }

  private fun handleFileEvent(event: Int, path: String?) {
    if ((event and (FileObserver.DELETE_SELF or FileObserver.MOVE_SELF)) != 0) {
      // 被监听的目录本身被删除或移动了，监听已失效，等待下次操作时重新监听
      _watchValid = false
      _cacheChangeCallback?.onCleared()
      return
    }

    if (path.isNullOrEmpty()) return

    val filename = path.removeSuffix(CACHE_SUFFIX_WITH_DOT)
    if (filename.length == path.length) return

    val key = filenameToKey(filename) ?: return

    when {
      // 文件被移出目录和被删除，对缓存来说是一回事
      (event and (FileObserver.DELETE or FileObserver.MOVED_FROM)) != 0 -> _cacheChangeCallback?.onRemove(key)
      (event and (FileObserver.MOVED_TO or FileObserver.CLOSE_WRITE)) != 0 -> _cacheChangeCallback?.onModify(key)
      else -> {}
    }
  }

  /** [key]对应的[File] */
  private fun fileOf(key: String): File {
    val keyBytes = keyToBytes(key)
    if (keyBytes.size > MAX_KEY_BYTES) {
      libException("Cache key is too long: ${keyBytes.size} bytes, max $MAX_KEY_BYTES bytes")
    }
    return _directory.resolve(keyToFilename(keyBytes) + CACHE_SUFFIX_WITH_DOT)
  }

  /**
   * 如果监听已失效，则检查目录并恢复监听。
   * 目录被删除后重建时（例如清除数据），之前的监听不会自动作用于新目录，必须重新监听。
   */
  private fun checkWatchValid() {
    if (!_watchValid) checkDirectoryExist()
  }

  /**
   * 检查目录是否存在，如果不存在则创建，并确保监听有效。
   *
   * 监听失效靠异步的DELETE_SELF事件感知，所以各操作发现目录不存在时也要调用本方法。
   * 只读路径[getCache]和[keys]尤其需要，否则监听失效后再也收不到事件。
   */
  private fun checkDirectoryExist() {
    val dir = _directory
    val shouldCreateDirectory = !dir.isDirectory
    if (shouldCreateDirectory) {
      if (dir.isFile) dir.delete()
      // 目录已不存在，之前的监听必然已失效
      _watchValid = false
      if (!dir.mkdirs() && !dir.isDirectory) {
        throw IOException("CacheStore mkdirs failure:$dir")
      }
    }
    val watching = startWatching()
    // 重新监听失败时也要通知，让订阅者重新读取，从而再次尝试监听
    if (shouldCreateDirectory || !watching) _cacheChangeCallback?.onCleared()
  }

  /** 确保监听有效，返回false表示注册期间目录被删除或替换，监听可能没有生效 */
  private fun startWatching(): Boolean {
    if (_watchValid) return true
    // 先标记为有效，注册期间收到的DELETE_SELF才能把它改回false
    _watchValid = true
    val watching = try {
      registerFileObserver()
    } catch (e: Throwable) {
      _watchValid = false
      throw e
    }
    if (!watching) _watchValid = false
    return watching
  }

  /**
   * 注册监听，返回false表示注册期间目录被删除或替换。
   *
   * 目录在注册前被删除时，注册会静默失败，也不会再收到DELETE_SELF，
   * 所以要比较注册前后目录的inode，而不能只依赖DELETE_SELF。
   */
  private fun registerFileObserver(): Boolean {
    // 注册期间保持目录打开，目录被删除后立即重建时，新目录不会复用它的inode
    val fd = try {
      Os.open(_directory.absolutePath, OsConstants.O_RDONLY, 0)
    } catch (e: ErrnoException) {
      if (e.isPathMissing()) return false
      // 无法判断目录是否变化，按监听有效处理，避免订阅者反复重新读取
      restartFileObserver()
      return true
    }
    try {
      val inode = Os.fstat(fd).st_ino
      restartFileObserver()
      return inode == directoryInode()
    } finally {
      Os.close(fd)
    }
  }

  private fun restartFileObserver() {
    // 先停止，清理掉可能残留的失效监听
    _fileObserver.stopWatching()
    _fileObserver.startWatching()
  }

  /** 目录当前的inode，无法获取时返回null */
  private fun directoryInode(): Long? {
    return try {
      Os.stat(_directory.absolutePath).st_ino
    } catch (_: ErrnoException) {
      null
    }
  }

  /** 删除临时文件 */
  private fun deleteTempFile() {
    _directory.listFiles { file ->
      file.name.startsWith(_tempFilePrefix) && file.name.endsWith(TEMP_SUFFIX_WITH_DOT)
    }?.forEach { it.delete() }
  }
}

/**
 * 只监听真正用到的事件。
 * 默认的ALL_EVENTS会让每次读缓存文件都产生OPEN/ACCESS/CLOSE_NOWRITE，白白唤醒观察线程。
 */
private const val FILE_OBSERVER_MASK = FileObserver.CLOSE_WRITE or
  FileObserver.MOVED_TO or
  FileObserver.MOVED_FROM or
  FileObserver.DELETE or
  FileObserver.DELETE_SELF or
  FileObserver.MOVE_SELF

/** 缓存文件后缀 */
private const val CACHE_SUFFIX_WITH_DOT = ".cache"
/** 临时文件后缀 */
private const val TEMP_SUFFIX_WITH_DOT = ".tmp"
/** 临时文件基础前缀，实际前缀还会包含进程名哈希 */
private const val TEMP_FILE_PREFIX = ".sd-cache-"

/**
 * key的最大字节数。
 * Linux下NAME_MAX为255字节，Base64编码后的长度为ceil(4n/3)，
 * 还要留出[CACHE_SUFFIX_WITH_DOT]的6个字节，
 * 即ceil(4n/3) <= 249，解得n <= 186。
 * 注意限制的是字节数不是字符数，UTF-8下一个汉字占3个字节。
 */
private const val MAX_KEY_BYTES = 186

/** 把[key]严格编码为UTF-8 */
private fun keyToBytes(key: String): ByteArray {
  return try {
    key.encodeToByteArray(throwOnInvalidSequence = true)
  } catch (e: CharacterCodingException) {
    libException("Cache key contains invalid UTF-16", e)
  }
}

/** 把[keyBytes]转为文件名 */
private fun keyToFilename(keyBytes: ByteArray): String {
  val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
  return Base64.encode(keyBytes, flag).decodeToString()
}

/** 把[filename]转为key */
private fun filenameToKey(filename: String): String? {
  return try {
    val input = filename.toByteArray()
    val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    // 非法UTF-8必须失败，不能静默替换成U+FFFD
    Base64.decode(input, flag)
      .also { bytes -> if (keyToFilename(bytes) != filename) return null }
      .decodeToString(throwOnInvalidSequence = true)
  } catch (_: Exception) {
    null
  }
}

private fun FileNotFoundException.isPathMissing(file: File): Boolean {
  val errnoException = cause as? ErrnoException ?: return !file.exists()
  return errnoException.isPathMissing()
}

private fun ErrnoException.isPathMissing(): Boolean {
  return errno == OsConstants.ENOENT || errno == OsConstants.ENOTDIR
}

/** 当前进程专属的临时文件前缀，按进程名区分，取不到进程名时用PID兜底 */
private fun tempFilePrefix(context: Context): String {
  val processName = context.currentProcessName()
  return "$TEMP_FILE_PREFIX${md5(processName)}-"
}

private fun Context.currentProcessName(): String {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    return Application.getProcessName()
  }

  val myPid = Process.myPid()
  val processName = try {
    (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
      ?.runningAppProcesses
      ?.firstOrNull { it.pid == myPid }
      ?.processName
  } catch (_: Exception) {
    null
  }?.takeIf { it.isNotBlank() }
  if (processName != null) return processName

  return try {
    File("/proc/self/cmdline").readText().trimEnd('\u0000')
  } catch (_: Exception) {
    null
  }?.takeIf { it.isNotBlank() } ?: "$packageName:${myPid}"
}
