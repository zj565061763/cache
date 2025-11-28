package com.sd.lib.cache.store

import android.content.Context
import android.os.FileObserver
import android.util.Base64
import java.io.File

abstract class DirectoryCacheStore : CacheStore {
  private lateinit var _directory: File
  private var _cacheChangeCallback: CacheStore.CacheChangeCallback? = null

  final override fun init(context: Context, directory: File) {
    if (::_directory.isInitialized) return
    _directory = directory
    checkDirectoryExist()
    _fileObserver.startWatching()
    initImpl(context, _directory)
  }

  final override fun putCache(key: String, value: ByteArray) {
    return putCacheImpl(fileOf(key), value)
  }

  final override fun getCache(key: String): ByteArray? {
    return getCacheImpl(fileOf(key))
  }

  final override fun removeCache(key: String) {
    removeCacheImpl(fileOf(key))
  }

  final override fun containsCache(key: String): Boolean {
    return containsCacheImpl(fileOf(key))
  }

  final override fun keys(): List<String> {
    val list = _directory.list()
    if (list.isNullOrEmpty()) return emptyList()
    return list.mapNotNull { filename ->
      filenameToKey(filename).also { key ->
        if (key == null) {
          runCatching { _directory.resolve(filename).deleteRecursively() }
        }
      }
    }
  }

  final override fun close() {
    _fileObserver.stopWatching()
    closeImpl()
  }

  final override fun setCacheChangeCallback(callback: CacheStore.CacheChangeCallback) {
    _cacheChangeCallback = callback
  }

  private val _fileObserver by lazy {
    object : FileObserver(_directory) {
      override fun onEvent(event: Int, path: String?) {
        val callback = _cacheChangeCallback
        if (callback != null && path != null) {
          when (event) {
            CREATE -> {
              filenameToKey(path)?.also { key ->
                callback.onCreate(key)
              }
            }
            MODIFY -> {
              filenameToKey(path)?.also { key ->
                callback.onModify(key)
              }
            }
            DELETE -> {
              filenameToKey(path)?.also { key ->
                callback.onRemove(key)
              }
            }
            else -> {}
          }
        }
      }
    }
  }

  /** [key]对应的[File] */
  private fun fileOf(key: String): File {
    val filename = keyToFilename(key)
    return _directory.resolve(filename)
  }

  protected open fun initImpl(context: Context, directory: File) = Unit
  protected open fun closeImpl() = Unit

  protected abstract fun putCacheImpl(file: File, value: ByteArray)
  protected abstract fun getCacheImpl(file: File): ByteArray?
  protected open fun removeCacheImpl(file: File) = file.deleteRecursively()
  protected open fun containsCacheImpl(file: File): Boolean = file.isFile

  /** 检查目录是否存在，如果不存在则创建 */
  protected fun checkDirectoryExist(): Boolean {
    val dir = _directory
    if (dir.isDirectory) return true
    if (dir.isFile) dir.delete()
    return dir.mkdirs()
  }
}

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