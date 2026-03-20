package com.sd.lib.cache.store

import android.content.Context
import android.os.FileObserver
import android.util.Base64
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

internal class FileCacheStore : CacheStore {
  private lateinit var _directory: File

  @Volatile
  private var _cacheChangeCallback: CacheStore.CacheChangeCallback? = null

  override fun init(context: Context, directory: File) {
    if (::_directory.isInitialized) return
    _directory = directory
    if (checkDirectoryExist()) {
      deleteTempFile()
      _fileObserver.startWatching()
    } else {
      throw IOException("mkdirs failure:$directory")
    }
  }

  override fun putCache(key: String, value: ByteArray) {
    val file = fileOf(key)
    val tempFile = file.resolveSibling("${file.name}${TEMP_SUFFIX_WITH_DOT}")

    fun writeWithTempFile() {
      tempFile.writeBytes(value)
      if (tempFile.renameTo(file)) {
        // 重命名成功
      } else {
        throw IOException("Rename failed from $tempFile to $file")
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
    return try {
      fileOf(key).readBytes()
    } catch (_: FileNotFoundException) {
      null
    }
  }

  override fun removeCache(key: String): Boolean {
    return fileOf(key).let { !it.exists() || it.delete() }
  }

  override fun keys(): List<String> {
    val listFile = _directory.listFiles { file -> file.name.endsWith(CACHE_SUFFIX_WITH_DOT) }
    if (listFile.isNullOrEmpty()) return emptyList()
    return listFile.mapNotNull { file ->
      val filename = file.name.removeSuffix(CACHE_SUFFIX_WITH_DOT)
      filenameToKey(filename)
    }
  }

  override fun destroy() {
    _fileObserver.stopWatching()
  }

  override fun setCacheChangeCallback(callback: CacheStore.CacheChangeCallback) {
    _cacheChangeCallback = callback
  }

  private val _fileObserver by lazy {
    object : FileObserver(_directory.absolutePath) {
      override fun onEvent(event: Int, path: String?) {
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

  /** 检查目录是否存在，如果不存在则创建 */
  private fun checkDirectoryExist(): Boolean {
    val dir = _directory
    if (dir.isDirectory) return true
    if (dir.isFile) dir.delete()
    return dir.mkdirs()
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
