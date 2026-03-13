package com.sd.lib.cache.store

import android.content.Context
import android.os.FileObserver
import android.util.Base64
import java.io.File

abstract class DirectoryCacheStore : CacheStore {
  private lateinit var _directory: File

  @Volatile
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
    val listFile = _directory.listFiles { it.isFile && it.name.endsWith(CACHE_SUFFIX_WITH_DOT) }
    if (listFile.isNullOrEmpty()) return emptyList()
    return listFile.mapNotNull { file ->
      val filename = file.name.removeSuffix(CACHE_SUFFIX_WITH_DOT)
      filenameToKey(filename)
    }
  }

  final override fun destroy() {
    _fileObserver.stopWatching()
    destroyImpl()
  }

  final override fun setCacheChangeCallback(callback: CacheStore.CacheChangeCallback) {
    _cacheChangeCallback = callback
  }

  private val _fileObserver by lazy {
    object : FileObserver(_directory.absolutePath) {
      override fun onEvent(event: Int, path: String?) {
        val callback = _cacheChangeCallback ?: return
        if (path.isNullOrEmpty()) return
        if (path.endsWith(CACHE_SUFFIX_WITH_DOT)) {
          val filename = path.removeSuffix(CACHE_SUFFIX_WITH_DOT)
          when {
            (event and CREATE) != 0 -> filenameToKey(filename)?.also { key -> callback.onCreate(key) }
            (event and DELETE) != 0 -> filenameToKey(filename)?.also { key -> callback.onRemove(key) }
            (event and MODIFY) != 0 -> filenameToKey(filename)?.also { key -> callback.onModify(key) }
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

  protected open fun initImpl(context: Context, directory: File) = Unit
  protected open fun destroyImpl() = Unit

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

/** 缓存文件后缀 */
private const val CACHE_SUFFIX_WITH_DOT = ".cache"

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