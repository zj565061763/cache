package com.sd.lib.cache.store

import android.content.Context
import android.os.FileObserver
import android.util.Base64
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

internal class FileCacheStore : CacheStore {
  private lateinit var _directory: File

  /** 监听是否有效 */
  @Volatile
  private var _watchValid = false

  @Volatile
  private var _cacheChangeCallback: CacheStore.CacheChangeCallback? = null

  override fun init(context: Context, directory: File) {
    if (::_directory.isInitialized) return
    _directory = directory
    if (checkDirectoryExist()) {
      deleteTempFile()
    } else {
      throw IOException("CacheStore mkdirs failure:$directory")
    }
  }

  override fun putCache(key: String, value: ByteArray) {
    checkWatchValid()
    val file = fileOf(key)
    val tempFile = file.resolveSibling("${file.name}${TEMP_SUFFIX_WITH_DOT}")

    fun writeWithTempFile() {
      tempFile.writeBytes(value)
      if (tempFile.renameTo(file)) {
        // 重命名成功
      } else {
        throw IOException("CacheStore.putCache rename failed from $tempFile to $file")
      }
    }

    try {
      writeWithTempFile()
    } catch (e: FileNotFoundException) {
      if (checkDirectoryExist()) {
        writeWithTempFile()
      } else {
        throw e
      }
    } finally {
      tempFile.delete()
    }
  }

  override fun getCache(key: String): ByteArray? {
    checkWatchValid()
    return try {
      fileOf(key).readBytes()
    } catch (_: FileNotFoundException) {
      // 文件不存在，也可能是整个目录被删除了
      checkDirectoryExist()
      null
    }
  }

  override fun removeCache(key: String): Boolean {
    checkWatchValid()
    val file = fileOf(key)
    if (!file.exists()) {
      // 文件不存在，也可能是整个目录被删除了
      checkDirectoryExist()
      return true
    }
    return file.delete()
  }

  override fun keys(): List<String> {
    checkWatchValid()
    val listFile = _directory.listFiles { file -> file.name.endsWith(CACHE_SUFFIX_WITH_DOT) }
    if (listFile == null) {
      // 目录不存在
      checkDirectoryExist()
      return emptyList()
    }
    if (listFile.isEmpty()) return emptyList()
    return listFile.mapNotNull { file ->
      val filename = file.name.removeSuffix(CACHE_SUFFIX_WITH_DOT)
      filenameToKey(filename)
    }
  }

  override fun setCacheChangeCallback(callback: CacheStore.CacheChangeCallback) {
    _cacheChangeCallback = callback
  }

  private val _fileObserver by lazy {
    object : FileObserver(_directory.absolutePath) {
      override fun onEvent(event: Int, path: String?) {
        if ((event and (DELETE_SELF or MOVE_SELF)) != 0) {
          // 被监听的目录本身被删除或移动了，监听已失效，等待下次操作时重新监听
          _watchValid = false
          return
        }

        if (path.isNullOrEmpty()) return

        val filename = path.removeSuffix(CACHE_SUFFIX_WITH_DOT)
        if (filename.length == path.length) return

        val key = filenameToKey(filename)
        if (key != null) {
          when {
            (event and DELETE) != 0 -> _cacheChangeCallback?.onRemove(key)
            (event and (MOVED_TO or CLOSE_WRITE)) != 0 -> _cacheChangeCallback?.onModify(key)
            else -> {}
          }
        }
      }
    }
  }

  /** [key]对应的[File] */
  private fun fileOf(key: String): File {
    val filename = keyToFilename(key)
    return _directory.resolve(filename + CACHE_SUFFIX_WITH_DOT)
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
   * 监听失效是靠DELETE_SELF事件感知的，而事件的投递是异步的，所以除了[checkWatchValid]，
   * 各个操作在发现目录不存在时也要调用本方法，否则目录刚被删除的那一小段时间里监听不会恢复。
   * 尤其是[getCache]和[keys]：只读的调用方只有这两条路径，它们不恢复的话，
   * 监听死了就再也收不到事件，也就再也没有机会恢复。
   */
  private fun checkDirectoryExist(): Boolean {
    val dir = _directory
    if (!dir.isDirectory) {
      if (dir.isFile) dir.delete()
      // 目录已不存在，之前的监听必然已失效
      _watchValid = false
      if (!dir.mkdirs()) return false
    }
    startWatching()
    return true
  }

  private fun startWatching() {
    if (_watchValid) return
    // 先停止，清理掉可能残留的失效监听
    _fileObserver.stopWatching()
    _fileObserver.startWatching()
    _watchValid = true
  }

  /** 删除临时文件 */
  private fun deleteTempFile() {
    _directory.listFiles { file -> file.name.endsWith(TEMP_SUFFIX_WITH_DOT) }?.forEach { it.delete() }
  }
}

/** 缓存文件后缀 */
private const val CACHE_SUFFIX_WITH_DOT = ".cache"
/** 临时文件后缀 */
private const val TEMP_SUFFIX_WITH_DOT = ".tmp"

/** 把[key]转为文件名 */
private fun keyToFilename(key: String): String {
  val input = key.toByteArray()
  val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
  return Base64.encode(input, flag).decodeToString()
}

/** 把[filename]转为key */
private fun filenameToKey(filename: String): String? {
  return runCatching {
    val input = filename.toByteArray()
    val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    Base64.decode(input, flag).decodeToString()
  }.getOrNull()
}
